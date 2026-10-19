package et.matchup.app

import et.matchup.app.data.AuthState
import et.matchup.app.data.UserDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.platform.consumeInitialLink
import et.matchup.app.platform.initialLink
import et.matchup.app.platform.isOnlineNow
import et.matchup.app.platform.isWebPlatform
import et.matchup.app.util.AuthLink
import et.matchup.app.util.parseAuthLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppState(
    val dependencies: AppContainer,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val auth: StateFlow<AuthState> = dependencies.session.state
    private val _online = MutableStateFlow(isOnlineNow())
    val online: StateFlow<Boolean> = _online.asStateFlow()
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val initialAuthLink: AuthLink? = parseAuthLink(initialLink())
    val isWeb: Boolean = isWebPlatform

    init {
        if (initialAuthLink != null) consumeInitialLink()
        scope.launch {
            while (true) {
                val current = isOnlineNow()
                if (_online.value != current) _online.value = current
                delay(4_000)
            }
        }
        scope.launch {
            auth.collect { state ->
                if (state is AuthState.SignedOut) {
                    dependencies.clearMemory()
                    _unreadCount.value = 0
                }
            }
        }
    }

    suspend fun bootstrap() {
        dependencies.session.bootstrap()
        if (auth.value is AuthState.SignedIn) pollUnread()
    }

    suspend fun login(email: String, password: String): UserDto {
        val user = dependencies.session.login(email, password)
        pollUnread()
        return user
    }

    suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        age: Int?,
        phone: String?,
    ): UserDto {
        val user = dependencies.session.signup(fullName, email, password, age, phone)
        pollUnread()
        return user
    }

    suspend fun signInWithGoogle(idToken: String): UserDto {
        val user = dependencies.session.signInWithGoogle(idToken)
        pollUnread()
        return user
    }

    suspend fun logout() {
        dependencies.session.logout()
        dependencies.clearMemory()
    }

    suspend fun logoutEverywhere() {
        dependencies.session.logoutEverywhere()
        dependencies.clearMemory()
    }

    suspend fun pollUnread() {
        if (auth.value !is AuthState.SignedIn || !online.value) return
        runCatching { dependencies.notifications.unreadCount() }
            .onSuccess { _unreadCount.value = it }
    }

    fun clearCachedData() {
        dependencies.cache.clear()
        dependencies.clearMemory()
        _unreadCount.value = 0
    }

    fun showMessage(message: String) {
        _messages.tryEmit(message)
    }

    fun showError(error: Throwable) {
        _messages.tryEmit(apiExceptionMessage(error))
    }

    fun close() {
        scope.cancel()
        dependencies.close()
    }
}
