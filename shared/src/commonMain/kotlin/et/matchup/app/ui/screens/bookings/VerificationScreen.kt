package et.matchup.app.ui.screens.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.BookingDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SecondaryButton
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.theme.MatchUpColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private enum class VerificationState { Checking, Confirmed, Review, Expired, Failed }

private fun verificationState(booking: BookingDto): VerificationState {
    val payment = booking.payments.lastOrNull()
    val paymentStatus = payment?.status.orEmpty().lowercase()
    return when {
        booking.status == "confirmed" || paymentStatus == "verified" -> VerificationState.Confirmed
        booking.status == "expired" -> VerificationState.Expired
        booking.status == "cancelled" -> VerificationState.Failed
        paymentStatus == "rejected" || paymentStatus == "failed" -> VerificationState.Failed
        paymentStatus == "pending" && booking.holdExpiresAt == null -> VerificationState.Review
        else -> VerificationState.Checking
    }
}

@Composable
fun VerificationScreen(
    state: AppState,
    bookingId: String,
    online: Boolean,
    onBack: () -> Unit,
    onViewBooking: () -> Unit,
    onBrowseGames: () -> Unit,
    onRetryPayment: () -> Unit,
) {
    val strings = LocalStrings.current
    var booking by remember { mutableStateOf<BookingDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf(0) }

    LaunchedEffect(bookingId) {
        while (isActive) {
            try {
                val loaded = state.dependencies.bookings.loadBooking(bookingId, force = true)
                booking = loaded.value
                error = null
                val resolved = verificationState(loaded.value) != VerificationState.Checking
                if (resolved || attempts >= 15) break
            } catch (throwable: Throwable) {
                error = apiExceptionMessage(throwable)
            }
            attempts += 1
            delay(4_000)
        }
    }

    val phase = booking?.let { verificationState(it) } ?: VerificationState.Checking

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.verifyingPayment, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (phase) {
                VerificationState.Checking -> {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(74.dp),
                            color = MatchUpColors.Lime,
                            strokeWidth = 3.dp,
                        )
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = MatchUpColors.Lime,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                    Spacer(Modifier.height(26.dp))
                    Text(
                        text = strings.verificationCheckingTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MatchUpColors.Text,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        text = strings.verificationCheckingBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MatchUpColors.TextMuted,
                        textAlign = TextAlign.Center,
                    )
                    if (error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = error.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MatchUpColors.Amber,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(26.dp))
                    SecondaryButton(
                        text = strings.viewBooking,
                        onClick = onViewBooking,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                VerificationState.Confirmed -> ResultBlock(
                    icon = Icons.Outlined.CheckCircle,
                    tint = MatchUpColors.Mint,
                    title = strings.verificationConfirmedTitle,
                    body = strings.verificationConfirmedBody,
                ) {
                    PrimaryButton(
                        text = strings.viewBooking,
                        onClick = onViewBooking,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton(
                        text = strings.backToDiscover,
                        onClick = onBrowseGames,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                VerificationState.Review -> ResultBlock(
                    icon = Icons.Outlined.Schedule,
                    tint = MatchUpColors.Purple,
                    title = strings.verificationReviewTitle,
                    body = strings.verificationReviewBody,
                ) {
                    PrimaryButton(
                        text = strings.viewBooking,
                        onClick = onViewBooking,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                VerificationState.Expired -> ResultBlock(
                    icon = Icons.Outlined.ErrorOutline,
                    tint = MatchUpColors.Amber,
                    title = strings.verificationExpiredTitle,
                    body = strings.verificationExpiredBody,
                ) {
                    PrimaryButton(
                        text = strings.backToDiscover,
                        onClick = onBrowseGames,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                VerificationState.Failed -> ResultBlock(
                    icon = Icons.Outlined.ErrorOutline,
                    tint = MatchUpColors.Red,
                    title = strings.verificationFailedTitle,
                    body = strings.verificationFailedBody,
                ) {
                    PrimaryButton(
                        text = strings.payNow,
                        onClick = onRetryPayment,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton(
                        text = strings.backToDiscover,
                        onClick = onBrowseGames,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (!online) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = strings.noConnectionBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextFaint,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ResultBlock(
    icon: ImageVector,
    tint: Color,
    title: String,
    body: String,
    actions: @Composable () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = tint.copy(alpha = if (MatchUpColors.isDark) 0.16f else 0.12f),
    ) {
        Box(Modifier.size(86.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(44.dp),
            )
        }
    }
    Spacer(Modifier.height(26.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MatchUpColors.Text,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(9.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MatchUpColors.TextMuted,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(28.dp))
    actions()
}
