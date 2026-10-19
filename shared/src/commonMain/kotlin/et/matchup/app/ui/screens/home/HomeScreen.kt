package et.matchup.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.matchup.app.AppState
import et.matchup.app.data.CatalogSnapshot
import et.matchup.app.data.GameDto
import et.matchup.app.data.UserDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.Avatar
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.FieldArt
import et.matchup.app.ui.components.FilterPill
import et.matchup.app.ui.components.IconBubble
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.MatchUpTextField
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.SecondaryButton
import et.matchup.app.ui.components.SurfaceCard
import et.matchup.app.ui.components.Wordmark
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.dateBucketLabel
import et.matchup.app.util.formatLabel
import et.matchup.app.util.formatLabelShort
import et.matchup.app.util.formatTime
import et.matchup.app.util.gameIsPast
import et.matchup.app.util.localDate
import et.matchup.app.util.moneyAmount
import et.matchup.app.util.savedAtLabel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private enum class DateFilter { All, Today, Tomorrow, ThisWeek }

@Composable
fun HomeScreen(
    state: AppState,
    user: UserDto,
    online: Boolean,
    unread: Int,
    onOpenGame: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val strings = LocalStrings.current
    var snapshot by remember { mutableStateOf<CatalogSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var savedAt by remember { mutableStateOf(0L) }
    var refreshKey by remember { mutableStateOf(0) }

    var search by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf(DateFilter.All) }
    var formatFilter by remember { mutableStateOf<String?>(null) }
    var fieldFilter by remember { mutableStateOf<String?>(null) }
    var availableOnly by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        if (snapshot == null) loading = true else refreshing = true
        error = null
        try {
            val result = state.dependencies.catalog.loadCatalog(force = refreshKey > 0)
            snapshot = result.value
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

    val today = Clock.System.now().localDate()
    val filtered = remember(snapshot, search, dateFilter, formatFilter, fieldFilter, availableOnly) {
        val query = search.trim().lowercase()
        val weekEnd = today.plus(7, DateTimeUnit.DAY)
        snapshot?.games.orEmpty()
            .filterNot { gameIsPast(it.startsAt, it.endsAt) }
            .filter { game ->
                val date = runCatching { Instant.parse(game.startsAt).localDate() }.getOrNull()
                val dateMatches = when (dateFilter) {
                    DateFilter.All -> true
                    DateFilter.Today -> date == today
                    DateFilter.Tomorrow -> date == today.plus(1, DateTimeUnit.DAY)
                    DateFilter.ThisWeek -> date != null && date >= today && date <= weekEnd
                }
                val queryMatches = query.isBlank() || listOf(
                    game.field.name,
                    game.field.location,
                    game.format,
                    formatLabel(game.format, strings),
                ).any { it.lowercase().contains(query) }
                val formatMatches = formatFilter == null || game.format == formatFilter
                val fieldMatches = fieldFilter == null || game.field.id == fieldFilter
                val availableMatches = !availableOnly || (!game.isFull && game.spotsRemaining > 0)
                dateMatches && queryMatches && formatMatches && fieldMatches && availableMatches
            }
            .sortedBy { it.startsAt }
    }

    val hasFilters = dateFilter != DateFilter.All ||
        formatFilter != null ||
        fieldFilter != null ||
        availableOnly
    val availableFields = snapshot?.fields.orEmpty()
    val availableFormats = snapshot?.games.orEmpty().map { it.format }.distinct().sorted()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Wordmark(markSize = 28.dp)
                Spacer(Modifier.weight(1f))
                NotificationButton(unread = unread, onClick = onOpenNotifications)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.clip(CircleShape).clickable(onClick = onOpenProfile)) {
                    Avatar(name = user.fullName.ifBlank { strings.appName }, size = 38.dp)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(14.dp))
                val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
                val timeOfDay = when {
                    hour < 12 -> strings.goodMorning
                    hour < 17 -> strings.goodAfternoon
                    else -> strings.goodEvening
                }
                val firstName = user.fullName.trim().split(" ").firstOrNull().orEmpty()
                    .ifBlank { strings.appName }
                Text(
                    text = strings.greeting(firstName, timeOfDay),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MatchUpColors.Text,
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MatchUpColors.Lime,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${strings.addisAbaba} · ${strings.homeSubtitle}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MatchUpColors.TextMuted,
                    )
                }
                Spacer(Modifier.height(20.dp))
                MatchUpTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = strings.searchPlaceholder,
                    leadingIcon = Icons.Outlined.Search,
                    trailingIcon = if (search.isNotEmpty()) {
                        {
                            IconButton(onClick = { search = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = strings.clearSearch,
                                    tint = MatchUpColors.TextMuted,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                    } else null,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterPill(
                        text = strings.today,
                        selected = dateFilter == DateFilter.Today,
                        onClick = {
                            dateFilter = if (dateFilter == DateFilter.Today) DateFilter.All else DateFilter.Today
                        },
                    )
                    FilterPill(
                        text = strings.tomorrow,
                        selected = dateFilter == DateFilter.Tomorrow,
                        onClick = {
                            dateFilter = if (dateFilter == DateFilter.Tomorrow) DateFilter.All else DateFilter.Tomorrow
                        },
                    )
                    FilterPill(
                        text = strings.availableOnly,
                        selected = availableOnly,
                        onClick = { availableOnly = !availableOnly },
                    )
                    FilterPill(
                        text = strings.filters,
                        selected = showFilters || hasFilters,
                        onClick = { showFilters = !showFilters },
                        icon = Icons.Outlined.FilterList,
                    )
                }
                if (showFilters) {
                    Spacer(Modifier.height(12.dp))
                    FilterPanel(
                        dateFilter = dateFilter,
                        formatFilter = formatFilter,
                        fieldFilter = fieldFilter,
                        availableOnly = availableOnly,
                        formats = availableFormats,
                        fields = availableFields.map { it.id to it.name },
                        onDate = { dateFilter = it },
                        onFormat = { formatFilter = it },
                        onField = { fieldFilter = it },
                        onAvailable = { availableOnly = it },
                        onClear = {
                            dateFilter = DateFilter.All
                            formatFilter = null
                            fieldFilter = null
                            availableOnly = false
                        },
                    )
                }
                Spacer(Modifier.height(16.dp))
                OfflineBanner(visible = !online)
                if (!online) Spacer(Modifier.height(12.dp))
                if (fromCache) {
                    Text(
                        text = strings.savedDataNotice.replace("%s", savedAtLabel(savedAt, strings)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.Amber,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (error != null && snapshot != null) {
                    InfoBanner(
                        title = strings.couldNotRefresh,
                        body = error.orEmpty(),
                        tint = MatchUpColors.Amber,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = strings.upcoming,
                            style = MaterialTheme.typography.titleLarge,
                            color = MatchUpColors.Text,
                        )
                        Text(
                            text = if (hasFilters || search.isNotBlank()) {
                                strings.upcomingGames(filtered.size)
                            } else {
                                strings.upcomingGames(snapshot?.games?.size ?: 0)
                            },
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
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = strings.refresh,
                                tint = if (online) MatchUpColors.TextMuted else MatchUpColors.TextFaint,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        when {
            loading && snapshot == null -> item {
                LoadingState(
                    text = strings.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )
            }
            error != null && snapshot == null -> item {
                ErrorState(
                    title = if (!online) strings.noConnectionTitle else strings.errorGeneric,
                    body = if (!online) strings.noConnectionBody else error.orEmpty(),
                    onRetry = { refreshKey += 1 },
                )
            }
            filtered.isEmpty() -> item {
                EmptyState(
                    title = if (hasFilters || search.isNotBlank()) {
                        strings.noFilterResultsTitle
                    } else {
                        strings.noGamesTitle
                    },
                    body = if (hasFilters || search.isNotBlank()) {
                        strings.noFilterResultsBody
                    } else {
                        strings.noGamesBody
                    },
                    icon = Icons.Outlined.SportsSoccer,
                    action = if (hasFilters || search.isNotBlank()) {
                        {
                            SecondaryButton(
                                text = strings.clearFilters,
                                onClick = {
                                    search = ""
                                    dateFilter = DateFilter.All
                                    formatFilter = null
                                    fieldFilter = null
                                    availableOnly = false
                                },
                            )
                        }
                    } else {
                        null
                    },
                )
            }
            else -> items(filtered, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                    onClick = { onOpenGame(game.id) },
                )
            }
        }
    }
}

@Composable
private fun NotificationButton(unread: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MatchUpColors.SurfaceElevated)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = LocalStrings.current.openNotifications,
            tint = MatchUpColors.Text,
            modifier = Modifier.size(21.dp),
        )
        if (unread > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 5.dp, end = 5.dp)
                    .size(15.dp)
                    .background(MatchUpColors.Lime, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unread > 9) "9+" else unread.toString(),
                    color = MatchUpColors.OnLime,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 8.sp,
                )
            }
        }
    }
}

@Composable
private fun FilterPanel(
    dateFilter: DateFilter,
    formatFilter: String?,
    fieldFilter: String?,
    availableOnly: Boolean,
    formats: List<String>,
    fields: List<Pair<String, String>>,
    onDate: (DateFilter) -> Unit,
    onFormat: (String?) -> Unit,
    onField: (String?) -> Unit,
    onAvailable: (Boolean) -> Unit,
    onClear: () -> Unit,
) {
    val strings = LocalStrings.current
    SurfaceCard {
        Text(
            text = strings.date,
            style = MaterialTheme.typography.labelMedium,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterPill(strings.allDates, dateFilter == DateFilter.All, onClick = { onDate(DateFilter.All) })
            FilterPill(strings.today, dateFilter == DateFilter.Today, onClick = { onDate(DateFilter.Today) })
            FilterPill(strings.tomorrow, dateFilter == DateFilter.Tomorrow, onClick = { onDate(DateFilter.Tomorrow) })
            FilterPill(strings.thisWeek, dateFilter == DateFilter.ThisWeek, onClick = { onDate(DateFilter.ThisWeek) })
        }
        if (formats.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = strings.format,
                style = MaterialTheme.typography.labelMedium,
                color = MatchUpColors.TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterPill(strings.allFormats, formatFilter == null, onClick = { onFormat(null) })
                formats.forEach { format ->
                    FilterPill(
                        text = formatLabel(format, strings),
                        selected = formatFilter == format,
                        onClick = { onFormat(format) },
                    )
                }
            }
        }
        if (fields.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = strings.field,
                style = MaterialTheme.typography.labelMedium,
                color = MatchUpColors.TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterPill(strings.allFields, fieldFilter == null, onClick = { onField(null) })
                fields.forEach { (id, name) ->
                    FilterPill(name, fieldFilter == id, onClick = { onField(id) })
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterPill(
                text = strings.availableOnly,
                selected = availableOnly,
                onClick = { onAvailable(!availableOnly) },
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = strings.clearFilters,
                style = MaterialTheme.typography.labelMedium,
                color = MatchUpColors.Lime,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClear)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun GameCard(
    game: GameDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val strings = LocalStrings.current
    val spots = game.spotsRemaining.coerceAtLeast(0)
    val full = game.isFull || spots == 0
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MatchUpColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MatchUpColors.OutlineSoft),
    ) {
        Column {
            FieldArt(
                seed = game.field.name + game.id,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Badge(
                        text = "${dateBucketLabel(game.startsAt, strings)} · ${formatTime(game.startsAt, strings)}",
                        color = MatchUpColors.Text,
                        background = Color.Black.copy(alpha = 0.42f),
                    )
                    Spacer(Modifier.weight(1f))
                    Badge(
                        text = if (full) strings.full else strings.spotsLeft(spots),
                        color = if (full) MatchUpColors.Red else MatchUpColors.Mint,
                        background = Color.Black.copy(alpha = 0.46f),
                    )
                }
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 13.dp),
                ) {
                    Text(
                        text = game.field.name.ifBlank { strings.venue },
                        style = MaterialTheme.typography.titleLarge,
                        color = MatchUpColors.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (game.field.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MatchUpColors.TextMuted,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = game.field.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MatchUpColors.TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = formatLabelShort(game.format),
                        style = MaterialTheme.typography.labelSmall,
                        color = MatchUpColors.Lime,
                        letterSpacing = 0.9.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MatchUpColors.TextFaint,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (game.durationMinutes > 0) {
                                "${strings.minutes(game.durationMinutes)} · ${strings.spots}: ${game.capacity}"
                            } else {
                                "${strings.spots}: ${game.capacity}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MatchUpColors.TextFaint,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = moneyAmount(game.pricePerPlayer),
                            style = MaterialTheme.typography.titleLarge,
                            color = MatchUpColors.Text,
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = strings.currency,
                            style = MaterialTheme.typography.labelSmall,
                            color = MatchUpColors.TextMuted,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                    Text(
                        text = strings.perPlayer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextFaint,
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color, background: Color) {
    Surface(
        shape = CircleShape,
        color = background,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.3.sp),
            color = color,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            maxLines = 1,
        )
    }
}
