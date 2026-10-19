package et.matchup.app.ui.screens.bookings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.BookingDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.FieldArt
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.SegmentedControl
import et.matchup.app.ui.components.StatusPill
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.dateBucketLabel
import et.matchup.app.util.formatDateTime
import et.matchup.app.util.formatTime
import et.matchup.app.util.gameIsPast
import et.matchup.app.util.moneyAmount
import et.matchup.app.util.savedAtLabel
import et.matchup.app.util.statusLabel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun BookingsScreen(
    state: AppState,
    online: Boolean,
    onOpenBooking: (String) -> Unit,
    onPay: (String) -> Unit,
) {
    val strings = LocalStrings.current
    var bookings by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var savedAt by remember { mutableStateOf(0L) }
    var refreshKey by remember { mutableStateOf(0) }
    var tab by remember { mutableStateOf("upcoming") }

    LaunchedEffect(refreshKey) {
        if (bookings.isEmpty()) loading = true else refreshing = true
        error = null
        try {
            val result = state.dependencies.bookings.loadBookings(force = refreshKey > 0)
            bookings = result.value
            fromCache = result.fromCache
            savedAt = result.savedAt
        } catch (throwable: Throwable) {
            error = apiExceptionMessage(throwable)
        } finally {
            loading = false
            refreshing = false
        }
    }
    LaunchedEffect(online) {
        if (online && fromCache) refreshKey += 1
    }

    val now = Clock.System.now()
    val upcoming = bookings.filter { booking ->
        val active = booking.status in setOf(
            "pending_payment",
            "verifying",
            "confirmed",
            "pending_review",
        )
        if (!active) return@filter false
        val lastGame = booking.games.lastOrNull()?.game
        val startsAt = lastGame?.startsAt ?: booking.createdAt
        val endsAt = lastGame?.endsAt ?: startsAt
        !gameIsPast(startsAt, endsAt)
    }.sortedBy { it.games.firstOrNull()?.game?.startsAt ?: it.createdAt }

    val past = bookings.filterNot { it in upcoming }
        .sortedByDescending { it.createdAt }

    val shown = if (tab == "upcoming") upcoming else past

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = strings.myBookings,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = strings.bookingsSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
            IconButton(
                onClick = { refreshKey += 1 },
                enabled = online && !refreshing && !loading,
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MatchUpColors.Lime,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = strings.refresh,
                        tint = if (online) MatchUpColors.TextMuted else MatchUpColors.TextFaint,
                    )
                }
            }
        }
        Column(Modifier.padding(horizontal = 18.dp)) {
            SegmentedControl(
                options = listOf(
                    "upcoming" to "${strings.upcoming} (${upcoming.size})",
                    "past" to "${strings.past} (${past.size})",
                ),
                selected = tab,
                onSelect = { tab = it },
            )
            Spacer(Modifier.height(12.dp))
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
            if (error != null && bookings.isNotEmpty()) {
                InfoBanner(
                    title = strings.couldNotRefresh,
                    body = error.orEmpty(),
                    tint = MatchUpColors.Amber,
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        Box(Modifier.weight(1f)) {
            when {
                loading && bookings.isEmpty() -> LoadingState()
                error != null && bookings.isEmpty() -> ErrorState(
                    title = if (!online) strings.noConnectionTitle else strings.errorGeneric,
                    body = if (!online) strings.noConnectionBody else error.orEmpty(),
                    onRetry = { refreshKey += 1 },
                )
                shown.isEmpty() -> EmptyState(
                    title = strings.noBookings,
                    body = strings.noBookingsBody,
                    icon = Icons.Outlined.ConfirmationNumber,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                ) {
                    items(shown, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            online = online,
                            onClick = { onOpenBooking(booking.id) },
                            onPay = { onPay(booking.id) },
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: BookingDto,
    online: Boolean,
    onClick: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val firstGame = booking.games.firstOrNull()?.game
    val startsAt = firstGame?.startsAt ?: booking.createdAt
    val spots = booking.spotCount
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MatchUpColors.Surface,
        border = BorderStroke(1.dp, MatchUpColors.OutlineSoft),
    ) {
        Row(Modifier.height(142.dp)) {
            FieldArt(
                seed = (firstGame?.field?.name ?: booking.id) + booking.id,
                modifier = Modifier
                    .width(104.dp)
                    .height(142.dp),
            ) {
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                ) {
                    Text(
                        text = dateBucketLabel(startsAt, strings),
                        style = MaterialTheme.typography.labelSmall,
                        color = MatchUpColors.Lime,
                    )
                    Text(
                        text = formatTime(startsAt, strings),
                        style = MaterialTheme.typography.titleSmall,
                        color = MatchUpColors.Text,
                    )
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        status = booking.status,
                        label = statusLabel(booking.status, strings),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = booking.referenceCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MatchUpColors.TextFaint,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = firstGame?.field?.name ?: strings.bookingNotFound,
                    style = MaterialTheme.typography.titleMedium,
                    color = MatchUpColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MatchUpColors.TextFaint,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatDateTime(startsAt, strings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${moneyAmount(booking.total)} ${strings.currency}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MatchUpColors.Text,
                        )
                        Text(
                            text = "$spots ${strings.spotWord(spots)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MatchUpColors.TextFaint,
                        )
                    }
                    if (booking.status == "pending_payment") {
                        Surface(
                            shape = CircleShape,
                            color = MatchUpColors.Lime.copy(alpha = if (online) 1f else 0.35f),
                            modifier = Modifier.clickable(enabled = online, onClick = onPay),
                        ) {
                            Text(
                                text = strings.payNow,
                                style = MaterialTheme.typography.labelMedium,
                                color = MatchUpColors.OnLime,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
