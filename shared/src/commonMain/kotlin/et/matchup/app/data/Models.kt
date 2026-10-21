package et.matchup.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ErrorEnvelope(
    val error: ErrorBody? = null,
)

@Serializable
data class ErrorBody(
    val code: String? = null,
    val message: String? = null,
    val fields: Map<String, String> = emptyMap(),
    @SerialName("request_id") val requestId: String? = null,
)

@Serializable
data class MessageDto(val message: String = "")

@Serializable
data class UserDto(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val email: String = "",
    val phone: String? = null,
    val role: String = "player",
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("has_password") val hasPassword: Boolean = false,
)

@Serializable
data class TokenPairDto(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("access_expires_at") val accessExpiresAt: String = "",
    @SerialName("refresh_expires_at") val refreshExpiresAt: String = "",
)

@Serializable
data class SessionDto(
    val user: UserDto = UserDto(),
    val tokens: TokenPairDto = TokenPairDto(),
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignupRequest(
    @SerialName("full_name") val fullName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
)

@Serializable
data class GoogleSignInRequest(
    @SerialName("id_token") val idToken: String,
)

@Serializable
data class PasswordResetRequest(val email: String)

@Serializable
data class PasswordResetConfirmRequest(
    val token: String,
    val password: String,
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class FieldDto(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class FieldListDto(val fields: List<FieldDto> = emptyList())

@Serializable
data class GameDto(
    val id: String = "",
    @SerialName("series_id") val seriesId: String? = null,
    @SerialName("starts_at") val startsAt: String = "",
    @SerialName("ends_at") val endsAt: String = "",
    @SerialName("duration_minutes") val durationMinutes: Int = 0,
    val format: String = "",
    @SerialName("price_per_player") val pricePerPlayer: String = "",
    @SerialName("price_per_player_santim") val pricePerPlayerSantim: Long = 0,
    val capacity: Int = 0,
    @SerialName("spots_remaining") val spotsRemaining: Int = 0,
    @SerialName("is_full") val isFull: Boolean = false,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    val status: String = "scheduled",
    val field: FieldDto = FieldDto(),
)

@Serializable
data class GameListDto(
    val games: List<GameDto> = emptyList(),
    val limit: Int = 20,
    val offset: Int = 0,
)

@Serializable
data class QuoteRequest(
    @SerialName("game_id") val gameId: String,
    @SerialName("booking_type") val bookingType: String,
    @SerialName("spot_count") val spotCount: Int,
)

@Serializable
data class QuoteWeekDto(
    @SerialName("game_id") val gameId: String = "",
    @SerialName("week_index") val weekIndex: Int = 0,
    @SerialName("starts_at") val startsAt: String = "",
    @SerialName("price_per_player") val pricePerPlayer: String = "",
    @SerialName("line_total") val lineTotal: String = "",
    @SerialName("spots_remaining") val spotsRemaining: Int = 0,
)

@Serializable
data class QuoteDto(
    @SerialName("booking_type") val bookingType: String = "single",
    @SerialName("spot_count") val spotCount: Int = 0,
    @SerialName("week_count") val weekCount: Int = 1,
    @SerialName("price_per_player") val pricePerPlayer: String = "",
    @SerialName("price_per_player_santim") val pricePerPlayerSantim: Long = 0,
    @SerialName("subtotal") val subtotal: String = "",
    @SerialName("subtotal_santim") val subtotalSantim: Long = 0,
    @SerialName("discount") val discount: String = "",
    @SerialName("discount_santim") val discountSantim: Long = 0,
    @SerialName("discount_label") val discountLabel: String? = null,
    @SerialName("total") val total: String = "",
    @SerialName("total_santim") val totalSantim: Long = 0,
    val weeks: List<QuoteWeekDto> = emptyList(),
)

@Serializable
data class ParticipantInputDto(
    @SerialName("slot_index") val slotIndex: Int,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class CreateBookingRequest(
    @SerialName("game_id") val gameId: String,
    @SerialName("booking_type") val bookingType: String,
    @SerialName("spot_count") val spotCount: Int,
    val participants: List<ParticipantInputDto> = emptyList(),
)

/**
 * The public Participant Go struct has no JSON tags, so the live API emits
 * "FullName" and "SlotIndex", while the generated OpenAPI document says
 * fullName/slotIndex. Decode both rather than depending on the document.
 */
@Serializable
data class ParticipantDto(
    @SerialName("FullName") val fullNameCaps: String? = null,
    @SerialName("SlotIndex") val slotIndexCaps: Int? = null,
    @SerialName("fullName") val fullNameLower: String? = null,
    @SerialName("slotIndex") val slotIndexLower: Int? = null,
) {
    fun displayName(): String = fullNameCaps ?: fullNameLower.orEmpty()
    fun index(): Int = slotIndexCaps ?: slotIndexLower ?: 0
}

@Serializable
data class BookedGameDto(
    @SerialName("week_index") val weekIndex: Int = 0,
    val game: GameDto = GameDto(),
)

@Serializable
data class PaymentViewDto(
    val id: String = "",
    val method: String = "",
    @SerialName("transaction_reference") val transactionReference: String = "",
    val status: String = "",
    @SerialName("expected_amount") val expectedAmount: String = "",
    @SerialName("verified_amount") val verifiedAmount: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("resolved_at") val resolvedAt: String? = null,
)

@Serializable
data class BookingDto(
    val id: String = "",
    @SerialName("reference_code") val referenceCode: String = "",
    @SerialName("booking_type") val bookingType: String = "single",
    val status: String = "pending_payment",
    @SerialName("spot_count") val spotCount: Int = 0,
    @SerialName("week_count") val weekCount: Int = 1,
    @SerialName("price_per_player") val pricePerPlayer: String = "",
    val subtotal: String = "",
    val discount: String = "",
    val total: String = "",
    @SerialName("total_santim") val totalSantim: Long = 0,
    @SerialName("hold_expires_at") val holdExpiresAt: String? = null,
    @SerialName("hold_seconds_remaining") val holdSecondsRemaining: Long? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    val games: List<BookedGameDto> = emptyList(),
    val participants: List<ParticipantDto> = emptyList(),
    val payments: List<PaymentViewDto> = emptyList(),
)

@Serializable
data class BookingListDto(
    val bookings: List<BookingDto> = emptyList(),
    val limit: Int = 20,
    val offset: Int = 0,
)

@Serializable
data class PaymentRequest(
    val method: String,
    @SerialName("transaction_reference") val transactionReference: String,
    @SerialName("account_suffix") val accountSuffix: String? = null,
    @SerialName("idempotency_key") val idempotencyKey: String,
)

@Serializable
data class PaymentSubmissionDto(
    @SerialName("payment_id") val paymentId: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    val method: String = "",
    @SerialName("transaction_reference") val transactionReference: String = "",
    val status: String = "",
    @SerialName("expected_amount") val expectedAmount: String = "",
    @SerialName("expected_amount_santim") val expectedAmountSantim: Long = 0,
    @SerialName("submitted_at") val submittedAt: String = "",
    val message: String = "",
)

@Serializable
data class PaymentMethodDto(
    val method: String = "",
    val label: String = "",
    val account: String = "",
    @SerialName("account_name") val accountName: String = "",
    val instructions: List<String> = emptyList(),
)

@Serializable
data class PaymentInstructionsDto(
    val methods: List<PaymentMethodDto> = emptyList(),
)

@Serializable
data class NotificationDto(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    val payload: JsonObject? = null,
)

@Serializable
data class NotificationListDto(
    val notifications: List<NotificationDto> = emptyList(),
    @SerialName("unread_count") val unreadCount: Int = 0,
    val limit: Int = 20,
    val offset: Int = 0,
)

@Serializable
data class ReadAllResultDto(
    @SerialName("marked_read") val markedRead: Int = 0,
)

// Proposed account endpoints. They are called by the app but the README calls
// out that the current backend must implement them for the UI to succeed.
@Serializable
data class UpdateProfileRequest(
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
)

@Serializable
data class EmailVerifyConfirmRequest(val token: String)

@Serializable
data class CatalogSnapshot(
    val fields: List<FieldDto> = emptyList(),
    val games: List<GameDto> = emptyList(),
)

data class CachedValue<T>(
    val value: T,
    val savedAt: Long,
    val fromCache: Boolean,
)
