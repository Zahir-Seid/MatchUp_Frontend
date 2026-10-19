package et.matchup.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.ApiException
import et.matchup.app.data.GameDto
import et.matchup.app.data.ParticipantInputDto
import et.matchup.app.data.QuoteDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.BottomBar
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.FieldArt
import et.matchup.app.ui.components.FilterPill
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.MatchUpTextField
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SegmentedControl
import et.matchup.app.ui.components.SpotCounter
import et.matchup.app.ui.components.SurfaceCard
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.formatDateLong
import et.matchup.app.util.formatLabel
import et.matchup.app.util.formatSantim
import et.matchup.app.util.formatTimeRange
import et.matchup.app.util.moneyAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameDetailScreen(
    state: AppState,
    gameId: String,
    online: Boolean,
    onBack: () -> Unit,
    onBooked: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var game by remember { mutableStateOf<GameDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var savedAt by remember { mutableStateOf(0L) }

    var bookingType by remember { mutableStateOf("single") }
    var spots by remember { mutableStateOf(1) }
    var names by remember { mutableStateOf<List<String>>(emptyList()) }
    var saved by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAddTeammate by remember { mutableStateOf(false) }
    var newTeammateName by remember { mutableStateOf("") }
    var quote by remember { mutableStateOf<QuoteDto?>(null) }
    var quoteLoading by remember { mutableStateOf(false) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var reserving by remember { mutableStateOf(false) }
    var reserveError by remember { mutableStateOf<String?>(null) }
    var loadKey by remember { mutableStateOf(0) }

    LaunchedEffect(gameId, loadKey) {
        loading = true
        error = null
        try {
            val result = state.dependencies.catalog.game(gameId)
            game = result.value
            fromCache = result.fromCache
            savedAt = result.savedAt
            spots = spots.coerceAtLeast(1)
        } catch (throwable: Throwable) {
            error = apiExceptionMessage(throwable)
        } finally {
            loading = false
        }
    }

    val userId = state.dependencies.session.currentUser()?.id
    LaunchedEffect(userId) {
        if (userId != null) {
            saved = state.dependencies.teammates.list(userId)
        }
    }

    val showMonthly = game?.let { it.isRecurring || !it.seriesId.isNullOrBlank() } == true
    val maxSpots = game?.spotsRemaining?.coerceAtMost(14)?.coerceAtLeast(0) ?: 0
    val full = game?.let { it.isFull || it.spotsRemaining <= 0 } == true

    LaunchedEffect(spots) {
        if (names.size < spots) {
            names = names + List(spots - names.size) { "" }
        } else if (names.size > spots) {
            names = names.take(spots)
        }
    }
    LaunchedEffect(showMonthly) {
        if (!showMonthly) bookingType = "single"
    }
    LaunchedEffect(game?.id, bookingType, spots, online) {
        val loaded = game ?: return@LaunchedEffect
        if (!online || full) {
            quote = null
            quoteLoading = false
            return@LaunchedEffect
        }
        delay(250)
        quoteLoading = true
        quoteError = null
        try {
            quote = state.dependencies.catalog.quote(loaded.id, bookingType, spots)
        } catch (throwable: Throwable) {
            quote = null
            quoteError = apiExceptionMessage(throwable)
        } finally {
            quoteLoading = false
        }
    }

    val fallbackTotal = game?.let { formatSantim(it.pricePerPlayerSantim * spots) }.orEmpty()
    val totalText = quote?.total ?: fallbackTotal

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.gameDetails, onBack = onBack)
        Box(Modifier.weight(1f)) {
            when {
                loading && game == null -> LoadingState()
                game == null -> ErrorState(
                    title = if (!online) strings.noConnectionTitle else strings.gameDetails,
                    body = error ?: if (!online) strings.noConnectionBody else strings.errorGeneric,
                    onRetry = { game = null; loading = true; error = null; loadKey += 1 },
                )
                else -> {
                    val loaded = game!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 22.dp),
                    ) {
                        item {
                            FieldArt(
                                seed = loaded.field.name + loaded.id,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(216.dp),
                            ) {
                                Column(
                                    Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                ) {
                                    Text(
                                        text = formatLabel(loaded.format, strings),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MatchUpColors.Lime,
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        text = loaded.field.name.ifBlank { strings.venue },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MatchUpColors.Text,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (loaded.field.location.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = MatchUpColors.TextMuted,
                                                modifier = Modifier.size(13.dp),
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = loaded.field.location,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MatchUpColors.TextMuted,
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(14.dp),
                                    shape = CircleShape,
                                    color = if (full) {
                                        MatchUpColors.Red.copy(alpha = 0.16f)
                                    } else {
                                        MatchUpColors.Mint.copy(alpha = 0.15f)
                                    },
                                ) {
                                    Text(
                                        text = if (full) strings.full else strings.spotsLeft(loaded.spotsRemaining),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (full) MatchUpColors.Red else MatchUpColors.Mint,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                        item {
                            Column(Modifier.padding(horizontal = 18.dp)) {
                                Spacer(Modifier.height(18.dp))
                                OfflineBanner(visible = !online)
                                if (!online) Spacer(Modifier.height(10.dp))
                                if (fromCache) {
                                    Text(
                                        text = strings.savedDataNotice
                                            .replace("%s", et.matchup.app.util.savedAtLabel(savedAt, strings)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchUpColors.Amber,
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                                if (reserveError != null) {
                                    InfoBanner(
                                        title = strings.errorGeneric,
                                        body = reserveError.orEmpty(),
                                        tint = MatchUpColors.Red,
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                if (full) {
                                    InfoBanner(
                                        title = strings.full,
                                        body = strings.gameFullBody,
                                        tint = MatchUpColors.Red,
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                SurfaceCard {
                                    DetailRow(
                                        icon = Icons.Outlined.CalendarMonth,
                                        label = strings.dateAndTime,
                                        value = "${formatDateLong(loaded.startsAt, strings)} · ${formatTimeRange(loaded.startsAt, loaded.endsAt, strings)}",
                                    )
                                    Spacer(Modifier.height(13.dp))
                                    DetailRow(
                                        icon = Icons.Outlined.Schedule,
                                        label = strings.duration,
                                        value = strings.minutes(loaded.durationMinutes),
                                    )
                                    Spacer(Modifier.height(13.dp))
                                    DetailRow(
                                        icon = Icons.Outlined.Groups,
                                        label = strings.spotsRemaining,
                                        value = loaded.spotsRemaining.toString(),
                                    )
                                    Spacer(Modifier.height(13.dp))
                                    DetailRow(
                                        icon = Icons.Outlined.Payments,
                                        label = strings.pricePerPlayer,
                                        value = "${moneyAmount(loaded.pricePerPlayer)} ${strings.currency}",
                                    )
                                }
                                Spacer(Modifier.height(18.dp))
                                if (showMonthly) {
                                    SegmentedControl(
                                        options = listOf(
                                            "single" to strings.singleGame,
                                            "monthly_4wk" to strings.monthlyPlan,
                                        ),
                                        selected = bookingType,
                                        onSelect = { bookingType = it },
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = strings.monthlyPlanHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchUpColors.TextFaint,
                                    )
                                    Spacer(Modifier.height(16.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = strings.spots,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MatchUpColors.Text,
                                        )
                                        Text(
                                            text = strings.teamNamesOptional,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MatchUpColors.TextFaint,
                                        )
                                    }
                                    SpotCounter(
                                        count = spots,
                                        max = maxOf(maxSpots, 1),
                                        onChange = { spots = it },
                                    )
                                }
                                Spacer(Modifier.height(18.dp))
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
                                            PriceRow(
                                                label = strings.pricePerPlayer,
                                                value = quote!!.pricePerPlayer,
                                            )
                                            if (spots > 1) {
                                                Spacer(Modifier.height(7.dp))
                                                PriceRow(
                                                    label = "${spots} × ${strings.spots}",
                                                    value = quote!!.subtotal,
                                                )
                                            }
                                            if (quote!!.discountSantim > 0) {
                                                Spacer(Modifier.height(7.dp))
                                                PriceRow(
                                                    label = quote!!.discountLabel ?: strings.discount,
                                                    value = "-${quote!!.discount}",
                                                    tint = MatchUpColors.Mint,
                                                )
                                            }
                                            Spacer(Modifier.height(10.dp))
                                            androidx.compose.material3.HorizontalDivider(
                                                color = MatchUpColors.OutlineSoft,
                                            )
                                            Spacer(Modifier.height(10.dp))
                                        }
                                        PriceRow(
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
                                            Spacer(Modifier.height(7.dp))
                                            Text(
                                                text = quoteError.orEmpty(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MatchUpColors.Amber,
                                            )
                                        }
                                        if (!online) {
                                            Spacer(Modifier.height(7.dp))
                                            Text(
                                                text = strings.offlineBookingBody,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MatchUpColors.TextFaint,
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(18.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = strings.teammates,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MatchUpColors.Text,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (userId != null) {
                                        Text(
                                            text = strings.addSavedTeammate,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MatchUpColors.Lime,
                                            modifier = Modifier
                                                .clickable {
                                                    newTeammateName = ""
                                                    showAddTeammate = true
                                                }
                                                .padding(vertical = 4.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = strings.teamNamesOptional,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MatchUpColors.TextFaint,
                                )
                                if (saved.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        saved.forEach { name ->
                                            FilterPill(
                                                text = name,
                                                selected = false,
                                                onClick = {
                                                    val index = names.indexOfFirst { it.isBlank() }
                                                    if (index >= 0) {
                                                        names = names.toMutableList().also { it[index] = name }
                                                    } else if (names.isEmpty()) {
                                                        names = listOf(name)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                repeat(spots) { index ->
                                    MatchUpTextField(
                                        value = names.getOrNull(index).orEmpty(),
                                        onValueChange = { value ->
                                            names = List(spots) { i ->
                                                if (i == index) value.take(120) else names.getOrNull(i).orEmpty()
                                            }
                                        },
                                        label = strings.spotLabel(index + 1),
                                        placeholder = strings.teammateName,
                                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                                    )
                                    if (index < spots - 1) Spacer(Modifier.height(10.dp))
                                }
                                Spacer(Modifier.height(16.dp))
                                InfoBanner(
                                    title = strings.holdNote,
                                    body = strings.paymentSafety,
                                    tint = MatchUpColors.Blue,
                                    icon = Icons.Outlined.SportsSoccer,
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
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
                    text = if (reserving) strings.reserving else strings.reserveSpots,
                    onClick = {
                        val loaded = game ?: return@PrimaryButton
                        if (!online) {
                            state.showMessage(strings.offlineBookingBody)
                            return@PrimaryButton
                        }
                        if (full) return@PrimaryButton
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
                                    bookingType = if (showMonthly) bookingType else "single",
                                    spotCount = spots,
                                    participants = participants,
                                )
                                state.dependencies.catalog.clearMemory()
                                onBooked(booking.id)
                            } catch (throwable: Throwable) {
                                reserveError = apiExceptionMessage(throwable)
                                state.showError(throwable)
                            } finally {
                                reserving = false
                            }
                        }
                    },
                    enabled = online && game != null && !full && !reserving && maxSpots > 0,
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

    if (showAddTeammate && userId != null) {
        AlertDialog(
            onDismissRequest = { showAddTeammate = false },
            title = { Text(strings.addSavedTeammate) },
            text = {
                MatchUpTextField(
                    value = newTeammateName,
                    onValueChange = { newTeammateName = it },
                    placeholder = strings.teammateNamePlaceholder,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newTeammateName.isNotBlank(),
                    onClick = {
                        val clean = newTeammateName.trim()
                        scope.launch {
                            saved = state.dependencies.teammates.add(userId, clean)
                            val index = names.indexOfFirst { it.isBlank() }
                            if (index >= 0) {
                                names = names.toMutableList().also { it[index] = clean }
                            }
                            state.showMessage(strings.teammateAdded)
                            showAddTeammate = false
                        }
                    },
                ) {
                    Text(strings.save, color = MatchUpColors.Lime)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeammate = false }) {
                    Text(strings.cancel, color = MatchUpColors.TextMuted)
                }
            },
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(32.dp)
                .padding(top = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MatchUpColors.Lime, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MatchUpColors.TextFaint)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MatchUpColors.Text)
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    big: Boolean = false,
    tint: Color = MatchUpColors.Text,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
