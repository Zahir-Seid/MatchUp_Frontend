package et.matchup.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class ApiException(
    val status: Int,
    val code: String? = null,
    override val message: String,
    val fields: Map<String, String> = emptyMap(),
) : Exception(message) {
    val isUnauthorized: Boolean get() = status == 401
    val isMissingEndpoint: Boolean get() = status == 404 || status == 405 || status == 501
}

fun apiExceptionMessage(error: Throwable): String = when (error) {
    is ApiException -> error.message
    else -> "Can't reach MatchUp. Check your connection and try again."
}

val AppJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    // Nulls in PATCH bodies mean "clear this field", so they must survive
    // serialization instead of being dropped as defaults.
    explicitNulls = true
    coerceInputValues = true
    encodeDefaults = false
}

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(AppJson)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 25_000
        connectTimeoutMillis = 12_000
        socketTimeoutMillis = 25_000
    }
    expectSuccess = false
}

/**
 * Thin, typed wrapper over the public MatchUp API. Every method is suspend; the
 * caller decides whether an authenticated call needs the session manager.
 */
class HttpApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val json: Json = AppJson,
) {
    private val root: String = baseUrl.trimEnd('/')

    fun close() = client.close()

    suspend fun login(email: String, password: String): SessionDto =
        post("/auth/login", LoginRequest(email = email.trim(), password = password))

    suspend fun signup(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
    ): SessionDto = post(
        "/auth/signup",
        SignupRequest(
            fullName = fullName.trim(),
            email = email.trim(),
            password = password,
            phone = phone?.trim()?.takeIf { it.isNotEmpty() },
        ),
    )

    suspend fun google(idToken: String): SessionDto =
        post("/auth/google", GoogleSignInRequest(idToken))

    suspend fun requestPasswordReset(email: String): MessageDto =
        post("/auth/password-reset/request", PasswordResetRequest(email.trim()))

    suspend fun confirmPasswordReset(token: String, password: String): MessageDto =
        post("/auth/password-reset/confirm", PasswordResetConfirmRequest(token.trim(), password))

    suspend fun refresh(refreshToken: String): TokenPairDto =
        post("/auth/refresh", RefreshRequest(refreshToken))

    suspend fun logout(refreshToken: String) {
        postNoContent("/auth/logout", RefreshRequest(refreshToken))
    }

    suspend fun logoutAll(accessToken: String) {
        postNoContent("/auth/logout-all", accessToken = accessToken)
    }

    suspend fun me(accessToken: String): UserDto =
        get("/me", accessToken = accessToken)

    suspend fun changePassword(
        accessToken: String,
        currentPassword: String,
        newPassword: String,
    ): TokenPairDto = post(
        "/auth/password/change",
        ChangePasswordRequest(currentPassword, newPassword),
        accessToken = accessToken,
    )

    suspend fun fields(): FieldListDto = get("/fields")

    suspend fun games(
        from: String,
        to: String,
        limit: Int = 100,
        offset: Int = 0,
    ): GameListDto {
        val query = buildString {
            append("/games?limit=").append(limit)
            append("&offset=").append(offset)
            append("&from=").append(from.encodeURLParameter())
            append("&to=").append(to.encodeURLParameter())
        }
        return get(query)
    }

    suspend fun game(id: String): GameDto = get("/games/${id.encodeURLParameter()}")

    suspend fun quote(accessToken: String, request: QuoteRequest): QuoteDto =
        post("/bookings/quote", request, accessToken = accessToken)

    suspend fun createBooking(accessToken: String, request: CreateBookingRequest): BookingDto =
        post("/bookings", request, accessToken = accessToken)

    suspend fun bookings(accessToken: String, limit: Int = 100, offset: Int = 0): BookingListDto =
        get("/bookings?limit=$limit&offset=$offset", accessToken = accessToken)

    suspend fun booking(accessToken: String, id: String): BookingDto =
        get("/bookings/${id.encodeURLParameter()}", accessToken = accessToken)

    suspend fun submitPayment(
        accessToken: String,
        bookingId: String,
        request: PaymentRequest,
    ): PaymentSubmissionDto = post(
        "/bookings/${bookingId.encodeURLParameter()}/payment",
        request,
        accessToken = accessToken,
    )

    suspend fun paymentInstructions(accessToken: String): PaymentInstructionsDto =
        get("/payment-instructions", accessToken = accessToken)

    suspend fun notifications(
        accessToken: String,
        unreadOnly: Boolean = false,
        limit: Int = 100,
        offset: Int = 0,
    ): NotificationListDto = get(
        "/notifications?unread=$unreadOnly&limit=$limit&offset=$offset",
        accessToken = accessToken,
    )

    suspend fun markNotificationRead(accessToken: String, id: String) {
        postNoContent("/notifications/${id.encodeURLParameter()}/read", accessToken = accessToken)
    }

    suspend fun markAllNotificationsRead(accessToken: String): ReadAllResultDto =
        post("/notifications/read-all", accessToken = accessToken)

    // ---- Account capabilities the current backend does not yet expose ------
    suspend fun updateProfile(accessToken: String, request: UpdateProfileRequest): UserDto {
        val response = execute(
            method = HttpMethod.Patch,
            path = "/me",
            accessToken = accessToken,
            body = request,
        )
        return readBody(response)
    }

    suspend fun requestEmailVerification(accessToken: String) {
        postNoContent("/auth/email/verify/request", accessToken = accessToken)
    }

    suspend fun confirmEmailVerification(accessToken: String, token: String): MessageDto =
        post("/auth/email/verify/confirm", EmailVerifyConfirmRequest(token), accessToken = accessToken)

    suspend fun cancelBooking(accessToken: String, bookingId: String): BookingDto =
        post("/bookings/${bookingId.encodeURLParameter()}/cancel", accessToken = accessToken)

    // ------------------------------------------------------------------------

    private suspend inline fun <reified Response> get(
        path: String,
        accessToken: String? = null,
    ): Response = readBody(execute(HttpMethod.Get, path, accessToken))

    private suspend inline fun <reified Request, reified Response> post(
        path: String,
        body: Request,
        accessToken: String? = null,
    ): Response = readBody(execute(HttpMethod.Post, path, accessToken, body))

    private suspend inline fun <reified Response> post(
        path: String,
        accessToken: String? = null,
    ): Response = readBody(execute(HttpMethod.Post, path, accessToken))

    private suspend inline fun <reified Request> postNoContent(
        path: String,
        body: Request,
        accessToken: String? = null,
    ) {
        execute(HttpMethod.Post, path, accessToken, body)
    }

    private suspend fun postNoContent(
        path: String,
        accessToken: String? = null,
    ) {
        execute(HttpMethod.Post, path, accessToken)
    }

    private suspend inline fun <reified Request> execute(
        method: HttpMethod,
        path: String,
        accessToken: String? = null,
        body: Request,
    ): HttpResponse {
        val response = try {
            client.request(root + path) {
                this.method = method
                if (accessToken != null) bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            throw networkError()
        }
        if (!response.status.isSuccess()) throw errorFrom(response)
        return response
    }

    private suspend fun execute(
        method: HttpMethod,
        path: String,
        accessToken: String? = null,
    ): HttpResponse {
        val response = try {
            client.request(root + path) {
                this.method = method
                if (accessToken != null) bearerAuth(accessToken)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            throw networkError()
        }
        if (!response.status.isSuccess()) throw errorFrom(response)
        return response
    }

    private fun networkError() = ApiException(
        status = 0,
        code = "network",
        message = "Can't reach MatchUp. Check your connection and try again.",
    )

    private suspend inline fun <reified T> readBody(response: HttpResponse): T {
        if (response.status == HttpStatusCode.NoContent) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }
        return response.body()
    }

    private suspend fun errorFrom(response: HttpResponse): ApiException {
        val raw = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
        val envelope = runCatching { json.decodeFromString<ErrorEnvelope>(raw) }.getOrNull()
        val body = envelope?.error
        return ApiException(
            status = response.status.value,
            code = body?.code,
            message = body?.message?.takeIf { it.isNotBlank() }
                ?: when (response.status.value) {
                    401 -> "Your session has expired. Please sign in again."
                    403 -> "You don't have permission to do that."
                    404 -> "We couldn't find that."
                    429 -> "Too many attempts. Please wait a moment and try again."
                    else -> "The server couldn't complete that request (${response.status.value})."
                },
            fields = body?.fields.orEmpty(),
        )
    }
}
