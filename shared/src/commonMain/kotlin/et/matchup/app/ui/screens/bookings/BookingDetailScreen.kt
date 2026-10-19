package et.matchup.app.ui.screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.ApiException
import et.matchup.app.data.BookingDto
import et.matchup.app.data.PaymentViewDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.BottomBar
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.FieldArt
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SecondaryButton
import et.matchup.app.ui.components.SurfaceCard
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.formatCountdown
import et.matchup.app.util.formatDateLong
import et.matchup.app.util.formatDateTime
import et.matchup.app.util.formatTimeRange
import et.matchup.app.util.holdRemainingSeconds
import et.matchup.app.util.moneyAmount
import et.matchup.app.util.savedAtLabel
import et.matchup.app.util.statusLabel
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// LocalClipboardManager is deprecated in favour of LocalClipboard, but the
// replacement needs a platform-specific ClipEntry there is no common way to
// build yet. Keep the working cross-platform API until one lands.
@Suppress("DEPRECATION")
@Composable
fun BookingDetailScreen(
    state: AppState,
    bookingId: String,
    online: Boolean,
    onBack: () -> Unit,
    onPay: (String) -> Unit,
    onBookAgain: (String?) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var booking by remember { mutableStateOf<BookingDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var savedAt by remember { mutableStateOf(0L) }
    var loadKey by remember { mutableStateOf(0) }
    var cancelling by remember { mutableStateOf(false) }
    var cancelDialog by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var remainingSeconds by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(bookingId, loadKey) {
        if (booking == null) loading = true
        error = null
        try {
            val result = state.dependencies.bookings.loadBooking(bookingId, force = loadKey > 0)
            booking = result.value
            fromCache = result.fromCache
            savedAt = result.savedAt
        } catch (throwable: Throwable) {
            error = apiExceptionMessage(throwable)
        } finally {
            loading = false
        }
    }
    LaunchedEffect(booking?.holdExpiresAt, booking?.holdSecondsRemaining) {
        remainingSeconds = holdRemainingSeconds(booking?.holdExpiresAt) ?: booking?.holdSecondsRemaining
        while ((remainingSeconds ?: 0) > 0) {
            delay(1_000)
            remainingSeconds = holdRemainingSeconds(booking?.holdExpiresAt)
                ?: ((remainingSeconds ?: 0) - 1).coerceAtLeast(0)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = strings.myBookings,
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = { loadKey += 1 },
                    enabled = online && !loading,
                ) {
                    if (!loading) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = strings.refresh,
                            tint = if (online) MatchUpColors.TextMuted else MatchUpColors.TextFaint,
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MatchUpColors.Lime,
                        )
                    }
                }
            },
        )
        Box(Modifier.weight(1f)) {
            when {
                loading && booking == null -> LoadingState()
                booking == null -> ErrorState(
                    title = if (!online) strings.noConnectionTitle else strings.bookingNotFound,
                    body = error ?: strings.errorGeneric,
                    onRetry = { loadKey += 1 },
                )
                else -> {
                    val current = booking!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    ) {
                        item {
                            OfflineBanner(visible = !online)
                            if (!online) Spacer(Modifier.height(10.dp))
                            if (fromCache) {
                                Text(
                                    text = strings.savedDataNotice.replace("%s", savedAtLabel(savedAt, strings)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MatchUpColors.Amber,
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            if (actionError != null) {
                                InfoBanner(
                                    title = strings.errorGeneric,
                                    body = actionError.orEmpty(),
                                    tint = MatchUpColors.Red,
                                    icon = Icons.Outlined.ErrorOutline,
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                            BookingHero(current)
                            Spacer(Modifier.height(14.dp))
                            when (current.status) {
                                "pending_payment" -> PendingStatusCard(
                                    booking = current,
                                    remainingSeconds = remainingSeconds,
                                    online = online,
                                    onPay = { onPay(current.id) },
                                )
                                "verifying" -> StatusNotice(
                                    title = strings.verifyingPayment,
                                    body = strings.paymentUnderReviewBody,
                                    tint = MatchUpColors.Blue,
                                    icon = Icons.Outlined.HourglassTop,
                                )
                                "confirmed" -> StatusNotice(
                                    title = strings.bookingConfirmed,
                                    body = strings.paymentConfirmedBody,
                                    tint = MatchUpColors.Mint,
                                    icon = Icons.Outlined.CheckCircle,
                                )
                                "pending_review" -> StatusNotice(
                                    title = strings.bookingPendingReview,
                                    body = strings.paymentUnderReviewBody,
                                    tint = MatchUpColors.Blue,
                                    icon = Icons.Outlined.HourglassTop,
                                )
                                "expired" -> StatusNotice(
                                    title = strings.bookingExpired,
                                    body = strings.holdExpiredBody,
                                    tint = MatchUpColors.Amber,
                                    icon = Icons.Outlined.HourglassTop,
                                )
                                "cancelled" -> StatusNotice(
                                    title = strings.bookingCancelled,
                                    body = strings.supportNote,
                                    tint = MatchUpColors.Red,
                                    icon = Icons.Outlined.ErrorOutline,
                                )
                            }
                            if (current.status in setOf("expired", "cancelled")) {
                                Spacer(Modifier.height(10.dp))
                                SecondaryButton(
                                    text = strings.bookAgain,
                                    onClick = { onBookAgain(current.games.firstOrNull()?.game?.id) },
                                )
                            }
                            if (current.status == "confirmed") {
                                Spacer(Modifier.height(14.dp))
                                CheckInPass(
                                    booking = current,
                                    onCopy = {
                                        clipboard.setText(AnnotatedString(current.referenceCode))
                                        state.showMessage(strings.referenceCopied)
                                    },
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            BookingDetailsCard(current)
                            if (current.games.size > 1) {
                                Spacer(Modifier.height(14.dp))
                                PlanCard(current)
                            }
                            if (current.payments.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                PaymentsCard(current.payments)
                            }
                            if (current.participants.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                ParticipantsCard(current)
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = strings.supportNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MatchUpColors.TextFaint,
                            )
                            if (current.status == "pending_payment") {
                                Spacer(Modifier.height(16.dp))
                                SecondaryButton(
                                    text = if (cancelling) strings.cancelling else strings.cancelReservation,
                                    onClick = { cancelDialog = true },
                                    enabled = online && !cancelling,
                                    danger = true,
                                )
                            }
                            Spacer(Modifier.height(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (cancelDialog) {
        AlertDialog(
            onDismissRequest = { if (!cancelling) cancelDialog = false },
            title = { Text(strings.cancelConfirmTitle) },
            text = { Text(strings.cancelConfirmBody) },
            confirmButton = {
                TextButton(
                    enabled = !cancelling,
                    onClick = {
                        scope.launch {
                            cancelling = true
                            actionError = null
                            try {
                                val updated = state.dependencies.bookings.cancelBooking(bookingId)
                                booking = updated
                                state.showMessage(strings.bookingCancelled)
                                cancelDialog = false
                            } catch (throwable: Throwable) {
                                if (throwable is ApiException && throwable.isMissingEndpoint) {
                                    actionError = strings.cancelUnsupported
                                } else {
                                    actionError = apiExceptionMessage(throwable)
                                }
                                cancelDialog = false
                            } finally {
                                cancelling = false
                            }
                        }
                    },
                ) {
                    Text(
                        text = if (cancelling) strings.cancelling else strings.cancelConfirm,
                        color = MatchUpColors.Red,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelDialog = false }, enabled = !cancelling) {
                    Text(strings.cancelKeep, color = MatchUpColors.TextMuted)
                }
            },
        )
    }
}

@Composable
private fun BookingHero(booking: BookingDto) {
    val strings = LocalStrings.current
    val firstGame = booking.games.firstOrNull()?.game
    FieldArt(
        seed = booking.id + (firstGame?.field?.name ?: ""),
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp)),
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(13.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.42f),
        ) {
            Text(
                text = booking.referenceCode,
                style = MaterialTheme.typography.labelSmall,
                color = MatchUpColors.Text,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = statusLabel(booking.status, strings).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = when (booking.status) {
                    "confirmed" -> MatchUpColors.Mint
                    "cancelled" -> MatchUpColors.Red
                    "expired" -> MatchUpColors.Amber
                    else -> MatchUpColors.Lime
                },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = firstGame?.field?.name ?: strings.gameDetails,
                style = MaterialTheme.typography.headlineMedium,
                color = MatchUpColors.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (firstGame != null) {
                Text(
                    text = formatDateTime(firstGame.startsAt, strings),
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun PendingStatusCard(
    booking: BookingDto,
    remainingSeconds: Long?,
    online: Boolean,
    onPay: () -> Unit,
) {
    val strings = LocalStrings.current
    SurfaceCard(
        color = MatchUpColors.Amber.copy(alpha = 0.06f),
        borderColor = MatchUpColors.Amber.copy(alpha = 0.2f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.HourglassTop,
                contentDescription = null,
                tint = MatchUpColors.Amber,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if ((remainingSeconds ?: 0) > 0) strings.bookingPendingPayment else strings.holdExpired,
                    style = MaterialTheme.typography.titleMedium,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = if ((remainingSeconds ?: 0) > 0) strings.holdNote else strings.holdExpiredBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
            if (remainingSeconds != null && remainingSeconds > 0) {
                Text(
                    text = formatCountdown(remainingSeconds),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MatchUpColors.Amber,
                )
            }
        }
        Spacer(Modifier.height(13.dp))
        PrimaryButton(
            text = strings.payNow,
            onClick = onPay,
            enabled = online && !booking.id.isBlank(),
        )
    }
}

@Composable
private fun StatusNotice(
    title: String,
    body: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    SurfaceCard(
        color = tint.copy(alpha = 0.06f),
        borderColor = tint.copy(alpha = 0.2f),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MatchUpColors.Text)
                Spacer(Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = MatchUpColors.TextMuted)
            }
        }
    }
}

@Composable
private fun CheckInPass(booking: BookingDto, onCopy: () -> Unit) {
    val strings = LocalStrings.current
    val painter = rememberQrCodePainter("MATCHUP:${booking.referenceCode}")
    SurfaceCard(color = MatchUpColors.SurfaceElevated) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.QrCode2,
                contentDescription = null,
                tint = MatchUpColors.Lime,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = strings.checkInPass,
                style = MaterialTheme.typography.titleMedium,
                color = MatchUpColors.Text,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = strings.copyReference,
                    tint = MatchUpColors.Lime,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painter,
                contentDescription = strings.checkInQr,
                modifier = Modifier.size(190.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = booking.referenceCode,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
            color = MatchUpColors.Text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = strings.showAtField,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = strings.checkinNote,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextFaint,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun BookingDetailsCard(booking: BookingDto) {
    val strings = LocalStrings.current
    SurfaceCard {
        Text(
            text = strings.paymentDetails,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(12.dp))
        DetailLine(strings.referenceCode, booking.referenceCode)
        Spacer(Modifier.height(8.dp))
        DetailLine(strings.status, statusLabel(booking.status, strings))
        Spacer(Modifier.height(8.dp))
        DetailLine(
            strings.bookingType,
            if (booking.bookingType == "monthly_4wk") strings.monthlyPlan else strings.singleGame,
        )
        Spacer(Modifier.height(8.dp))
        DetailLine(strings.spots, "${booking.spotCount}")
        Spacer(Modifier.height(8.dp))
        DetailLine("${strings.total}", "${moneyAmount(booking.total)} ${strings.currency}")
        booking.games.firstOrNull()?.game?.let { game ->
            Spacer(Modifier.height(8.dp))
            DetailLine(strings.venue, game.field.name)
            Spacer(Modifier.height(8.dp))
            DetailLine(strings.dateAndTime, "${formatDateLong(game.startsAt, strings)} · ${formatTimeRange(game.startsAt, game.endsAt, strings)}")
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextFaint,
            modifier = Modifier.width(118.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.Text,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PlanCard(booking: BookingDto) {
    val strings = LocalStrings.current
    SurfaceCard {
        Text(
            text = strings.gamesInPlan,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(10.dp))
        booking.games.sortedBy { it.weekIndex }.forEachIndexed { index, booked ->
            if (index > 0) {
                Spacer(Modifier.height(9.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MatchUpColors.OutlineSoft),
                )
                Spacer(Modifier.height(9.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(28.dp)
                        .background(MatchUpColors.SurfaceHigh, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (booked.weekIndex + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MatchUpColors.Lime,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = booked.game.field.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MatchUpColors.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatDateTime(booked.game.startsAt, strings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentsCard(payments: List<PaymentViewDto>) {
    val strings = LocalStrings.current
    SurfaceCard {
        Text(
            text = strings.paymentHistory,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
        )
        payments.forEachIndexed { index, payment ->
            if (index > 0) {
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MatchUpColors.OutlineSoft),
                )
                Spacer(Modifier.height(10.dp))
            } else {
                Spacer(Modifier.height(10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = MatchUpColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = payment.method.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MatchUpColors.Text,
                    )
                    Text(
                        text = payment.transactionReference,
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = payment.status.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (payment.status) {
                            "verified", "confirmed" -> MatchUpColors.Mint
                            "rejected", "failed" -> MatchUpColors.Red
                            else -> MatchUpColors.Amber
                        },
                    )
                    Text(
                        text = "${moneyAmount(payment.expectedAmount)} ${strings.currency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextFaint,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantsCard(booking: BookingDto) {
    val strings = LocalStrings.current
    SurfaceCard {
        Text(
            text = strings.participants,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(8.dp))
        booking.participants.sortedBy { it.index() }.forEach { participant ->
            val name = participant.displayName()
            if (name.isNotBlank()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.spotLabel(participant.index() + 1),
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextFaint,
                        modifier = Modifier.width(72.dp),
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MatchUpColors.Text,
                    )
                }
            }
        }
    }
}
