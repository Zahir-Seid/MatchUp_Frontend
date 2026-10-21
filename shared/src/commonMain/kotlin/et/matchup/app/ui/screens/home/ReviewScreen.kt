package et.matchup.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.GameDto
import et.matchup.app.data.ParticipantInputDto
import et.matchup.app.data.QuoteDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.BottomBar
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SurfaceCard
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.formatDateLong
import et.matchup.app.util.formatLabel
import et.matchup.app.util.formatTimeRange
import et.matchup.app.util.moneyAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReviewScreen(
    state: AppState,
    gameId: String,
    bookingType: String,
    spots: Int,
    names: List<String>,
    online: Boolean,
    onBack: () -> Unit,
    onReserved: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var game by remember { mutableStateOf<GameDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var quote by remember { mutableStateOf<QuoteDto?>(null) }
    var quoteLoading by remember { mutableStateOf(true) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var reserving by remember { mutableStateOf(false) }
    var reserveError by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }

    LaunchedEffect(gameId, retryKey) {
        loading = true
        loadError = null
        try {
            game = state.dependencies.catalog.game(gameId).value
        } catch (throwable: Throwable) {
            loadError = apiExceptionMessage(throwable)
        } finally {
            loading = false
        }
    }

    LaunchedEffect(gameId, bookingType, spots, online, retryKey) {
        if (!online) {
            quoteLoading = false
            return@LaunchedEffect
        }
        quoteLoading = true
        quoteError = null
        delay(150)
        try {
            quote = state.dependencies.catalog.quote(gameId, bookingType, spots)
        } catch (throwable: Throwable) {
            quote = null
            quoteError = apiExceptionMessage(throwable)
        } finally {
            quoteLoading = false
        }
    }

    val totalText = quote?.total ?: game?.let {
        moneyAmount(et.matchup.app.util.formatSantim(it.pricePerPlayerSantim * spots))
    }.orEmpty()

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.reviewHoldTitle, onBack = onBack)
        when {
            loading && game == null -> LoadingState(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            game == null -> ErrorState(
                title = if (!online) strings.noConnectionTitle else strings.errorGeneric,
                body = loadError ?: strings.errorGeneric,
                onRetry = { retryKey += 1 },
                modifier = Modifier.weight(1f),
            )
            else -> {
                val loaded = game!!
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 8.dp,
                        bottom = 20.dp,
                    ),
                ) {
                    item {
                        OfflineBanner(visible = !online)
                        if (!online) Spacer(Modifier.height(10.dp))
                        if (reserveError != null) {
                            InfoBanner(
                                title = strings.errorGeneric,
                                body = reserveError.orEmpty(),
                                tint = MatchUpColors.Red,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Text(
                            text = strings.reviewHoldSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MatchUpColors.TextMuted,
                        )
                        Spacer(Modifier.height(16.dp))
                        SurfaceCard {
                            Text(
                                text = loaded.field.name.ifBlank { strings.venue },
                                style = MaterialTheme.typography.titleLarge,
                                color = MatchUpColors.Text,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (loaded.field.location.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = MatchUpColors.TextFaint,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = loaded.field.location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchUpColors.TextMuted,
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = MatchUpColors.Lime,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    text = "${formatDateLong(loaded.startsAt, strings)} · " +
                                        formatTimeRange(loaded.startsAt, loaded.endsAt, strings),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MatchUpColors.Text,
                                )
                            }
                            Spacer(Modifier.height(9.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Groups,
                                    contentDescription = null,
                                    tint = MatchUpColors.Lime,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    text = "${spots} × ${strings.spots} · ${formatLabel(loaded.format, strings)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MatchUpColors.Text,
                                )
                            }
                            val named = names.map { it.trim() }.filter { it.isNotBlank() }
                            if (named.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = named.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MatchUpColors.TextMuted,
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        SurfaceCard(color = MatchUpColors.SurfaceElevated) {
                            if (quoteLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MatchUpColors.Lime,
                                    )
                                    Spacer(Modifier.width(9.dp))
                                    Text(
                                        text = strings.quoteLoading,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchUpColors.TextMuted,
                                    )
                                }
                            } else {
                                if (quote != null) {
                                    ReviewPriceRow(
                                        label = strings.pricePerPlayer,
                                        value = quote!!.pricePerPlayer,
                                    )
                                    if (spots > 1) {
                                        Spacer(Modifier.height(8.dp))
                                        ReviewPriceRow(
                                            label = "$spots × ${strings.spots}",
                                            value = quote!!.subtotal,
                                        )
                                    }
                                    if (quote!!.discountSantim > 0) {
                                        Spacer(Modifier.height(8.dp))
                                        ReviewPriceRow(
                                            label = quote!!.discountLabel ?: strings.discount,
                                            value = "-${quote!!.discount}",
                                            tint = MatchUpColors.Mint,
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    HorizontalDivider(color = MatchUpColors.OutlineSoft)
                                    Spacer(Modifier.height(10.dp))
                                }
                                ReviewPriceRow(
                                    label = strings.total,
                                    value = totalText,
                                    big = true,
                                )
                                if (quote?.weekCount != null && quote!!.weekCount > 1) {
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        text = "${quote!!.weekCount} × ${strings.weeklySessions}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchUpColors.TextFaint,
                                    )
                                }
                                if (quoteError != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = quoteError.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MatchUpColors.Amber,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(
                                            onClick = { retryKey += 1 },
                                            enabled = online,
                                        ) {
                                            Text(
                                                text = strings.retry,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MatchUpColors.Lime,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        InfoBanner(
                            title = strings.holdNote,
                            body = strings.paymentSafety,
                            tint = MatchUpColors.Blue,
                            icon = Icons.Outlined.SportsSoccer,
                        )
                    }
                }
                BottomBar {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = strings.total,
                                style = MaterialTheme.typography.bodySmall,
                                color = MatchUpColors.TextMuted,
                            )
                            Text(
                                text = moneyAmount(totalText) + " ${strings.currency}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MatchUpColors.Text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        PrimaryButton(
                            text = if (reserving) strings.reserving else strings.confirmAndPay,
                            onClick = {
                                if (!online) {
                                    state.showMessage(strings.offlineBookingBody)
                                    return@PrimaryButton
                                }
                                reserveError = null
                                scope.launch {
                                    reserving = true
                                    try {
                                        val participants = names.mapIndexedNotNull { index, name ->
                                            name.trim().takeIf { it.isNotBlank() }?.let {
                                                ParticipantInputDto(slotIndex = index + 1, fullName = it)
                                            }
                                        }
                                        val booking = state.dependencies.bookings.createBooking(
                                            gameId = loaded.id,
                                            bookingType = bookingType,
                                            spotCount = spots,
                                            participants = participants,
                                        )
                                        state.dependencies.catalog.clearMemory()
                                        onReserved(booking.id)
                                    } catch (throwable: Throwable) {
                                        reserveError = apiExceptionMessage(throwable)
                                        state.showError(throwable)
                                    } finally {
                                        reserving = false
                                    }
                                }
                            },
                            enabled = online && !reserving && quote != null && !quoteLoading,
                            loading = reserving,
                            modifier = Modifier.weight(1.05f),
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = strings.holdNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextFaint,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewPriceRow(
    label: String,
    value: String,
    big: Boolean = false,
    tint: androidx.compose.ui.graphics.Color = MatchUpColors.Text,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (big) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (big) MatchUpColors.Text else MatchUpColors.TextMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (big) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            color = tint,
            fontWeight = if (big) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
