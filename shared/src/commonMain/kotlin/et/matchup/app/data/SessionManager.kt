package et.matchup.app.data

import et.matchup.app.platform.KeyValueStore
import et.matchup.app.platform.isOnlineNow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: UserDto) : AuthState
}

/**
 * Owns the token pair and is the only place that may rotate it. Refresh tokens
 * are single-use, so every refresh is serialized behind [refreshMutex] and the
 * caller's access token is checked before spending the refresh token.
 */
class SessionManager(
    private val api: HttpApi,
    private val store: KeyValueStore,
    private val cache: JsonCache,
    private val json: Json = AppJson,
) {
    private val refreshMutex = Mutex()
    private var tokens: TokenPairDto? = loadTokens()

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun currentUser(): UserDto? = (state.value as? AuthState.SignedIn)?.user

    suspend fun bootstrap() {
        if (state.value !is AuthState.Loading) return
        val pair = tokens
        val cachedUser = loadCachedUser()
        if (pair == null || pair.accessToken.isBlank()) {
            tokens = null
            store.remove(tokensKey)
            _state.value = AuthState.SignedOut
            return
        }
        if (cachedUser != null) {
            _state.value = AuthState.SignedIn(cachedUser)
        }
        if (!isOnlineNow()) {
            if (cachedUser == null) _state.value = AuthState.SignedOut
            return
        }
        try {
            val user = authenticated { access -> api.me(access) }
            persistUser(user)
            _state.value = AuthState.SignedIn(user)
        } catch (error: ApiException) {
            if (error.status == 401) {
                clearLocal()
            } else if (cachedUser == null) {
                _state.value = AuthState.SignedOut
            }
        } catch (_: Throwable) {
            if (cachedUser == null) _state.value = AuthState.SignedOut
        }
    }

    suspend fun login(email: String, password: String): UserDto {
        val session = api.login(email, password)
        accept(session)
        return session.user
    }

    suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
    ): UserDto {
        val session = api.signup(fullName, email, password, phone)
        accept(session)
        return session.user
    }

    suspend fun signInWithGoogle(idToken: String): UserDto {
        val session = api.google(idToken)
        accept(session)
        return session.user
    }

    suspend fun requestPasswordReset(email: String): String =
        api.requestPasswordReset(email).message

    suspend fun confirmPasswordReset(token: String, password: String): String =
        api.confirmPasswordReset(token, password).message

    suspend fun logout() {
        val refresh = tokens?.refreshToken
        if (!refresh.isNullOrBlank() && isOnlineNow()) {
            runCatching { api.logout(refresh) }
        }
        clearLocal()
    }

    suspend fun logoutEverywhere() {
        if (isOnlineNow()) {
            runCatching { authenticated { api.logoutAll(it) } }
        }
        clearLocal()
    }

    suspend fun refreshProfile(): UserDto {
        val user = authenticated { api.me(it) }
        persistUser(user)
        _state.value = AuthState.SignedIn(user)
        return user
    }

    suspend fun updateProfile(request: UpdateProfileRequest): UserDto {
        val user = authenticated { api.updateProfile(it, request) }
        persistUser(user)
        _state.value = AuthState.SignedIn(user)
        return user
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val pair = authenticated { api.changePassword(it, currentPassword, newPassword) }
        tokens = pair
        store.putString(tokensKey, json.encodeToString(TokenPairDto.serializer(), pair))
        val user = currentUser()?.copy(hasPassword = true)
        if (user != null) {
            persistUser(user)
            _state.value = AuthState.SignedIn(user)
        }
    }

    suspend fun requestEmailVerification() {
        authenticated { api.requestEmailVerification(it) }
    }

    suspend fun confirmEmailVerification(token: String): String =
        authenticated { api.confirmEmailVerification(it, token).message }

    /** Runs an authenticated API block, rotating the token once when needed. */
    suspend fun <T> authenticated(block: suspend (String) -> T): T {
        var token = accessToken()
        return try {
            block(token)
        } catch (error: ApiException) {
            if (error.status != 401) throw error
            if (!refreshTokens(token)) {
                clearLocal()
                throw error
            }
            token = tokens?.accessToken ?: throw unauthorized()
            try {
                block(token)
            } catch (second: ApiException) {
                if (second.status == 401) clearLocal()
                throw second
            }
        }
    }

    fun clearLocal() {
        tokens = null
        store.remove(tokensKey)
        store.remove(userKey)
        cache.clear()
        _state.value = AuthState.SignedOut
    }

    private suspend fun accessToken(): String {
        val pair = tokens ?: throw unauthorized()
        if (pair.accessToken.isBlank()) throw unauthorized()
        val now = Clock.System.now().toEpochMilliseconds()
        val expiresAt = parseInstant(pair.accessExpiresAt)?.toEpochMilliseconds()
        val expiringSoon = expiresAt != null && expiresAt - now < 60_000
        if (expiringSoon && isOnlineNow() && pair.refreshToken.isNotBlank()) {
            val stillValid = expiresAt > now
            try {
                if (!refreshTokens(pair.accessToken) && !stillValid) throw unauthorized()
            } catch (error: Throwable) {
                // A network hiccup must not throw away a token that still works.
                if (!stillValid) throw error
            }
        }
        return tokens?.accessToken ?: throw unauthorized()
    }

    private suspend fun refreshTokens(usedAccessToken: String): Boolean = refreshMutex.withLock {
        val current = tokens ?: return@withLock false
        if (current.accessToken != usedAccessToken) return@withLock true
        if (current.refreshToken.isBlank()) return@withLock false
        if (!isOnlineNow()) return@withLock false
        try {
            val pair = api.refresh(current.refreshToken)
            tokens = pair
            store.putString(tokensKey, json.encodeToString(TokenPairDto.serializer(), pair))
            true
        } catch (error: ApiException) {
            if (error.status == 401 || error.status == 422) {
                clearLocal()
                false
            } else {
                throw error
            }
        }
    }

    private fun accept(session: SessionDto) {
        val previous = loadCachedUser()
        if (previous != null && previous.id != session.user.id) cache.clear()
        tokens = session.tokens
        store.putString(tokensKey, json.encodeToString(TokenPairDto.serializer(), session.tokens))
        persistUser(session.user)
        _state.value = AuthState.SignedIn(session.user)
    }

    private fun persistUser(user: UserDto) {
        store.putString(userKey, json.encodeToString(UserDto.serializer(), user))
    }

    private fun loadTokens(): TokenPairDto? {
        val raw = store.getString(tokensKey) ?: return null
        return runCatching { json.decodeFromString(TokenPairDto.serializer(), raw) }.getOrNull()
    }

    private fun loadCachedUser(): UserDto? {
        val raw = store.getString(userKey) ?: return null
        return runCatching { json.decodeFromString(UserDto.serializer(), raw) }.getOrNull()
    }

    private fun parseInstant(value: String): Instant? =
        runCatching { Instant.parse(value) }.getOrNull()

    private fun unauthorized() = ApiException(
        status = 401,
        code = "unauthorized",
        message = "Please sign in again.",
    )

    private companion object {
        const val tokensKey = "auth.tokens"
        const val userKey = "auth.user"
    }
}
