package et.matchup.app.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.NotificationDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.IconBubble
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.relativeTime
import et.matchup.app.util.savedAtLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun NotificationsScreen(
    state: AppState,
    online: Boolean,
    onOpenBooking: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var unreadCount by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var savedAt by remember { mutableStateOf(0L) }
    var refreshKey by remember { mutableStateOf(0) }
    var markingAll by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        if (notifications.isEmpty()) loading = true else refreshing = true
        error = null
        try {
            val result = state.dependencies.notifications.load(force = refreshKey > 0)
            notifications = result.value.notifications
            unreadCount = result.value.unreadCount
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
    LaunchedEffect(online) {
        if (!online) return@LaunchedEffect
        while (true) {
            delay(25_000)
            refreshKey += 1
            state.pollUnread()
        }
    }

    fun bookingId(notification: NotificationDto): String? =
        notification.payload?.get("booking_id")?.jsonPrimitive?.contentOrNull
            ?: notification.payload?.get("bookingId")?.jsonPrimitive?.contentOrNull

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.notifications,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MatchUpColors.Text,
                    )
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(9.dp))
                        Surface(shape = CircleShape, color = MatchUpColors.Lime) {
                            Text(
                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MatchUpColors.OnLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
                Text(
                    text = if (unreadCount > 0) "${unreadCount} ${strings.unread.lowercase()}" else strings.allRead,
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
        if (unreadCount > 0) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    enabled = online && !markingAll,
                    onClick = {
                        scope.launch {
                            markingAll = true
                            try {
                                state.dependencies.notifications.markAllRead()
                                notifications = notifications.map { it.copy(read = true) }
                                unreadCount = 0
                                state.pollUnread()
                            } catch (throwable: Throwable) {
                                state.showError(throwable)
                            } finally {
                                markingAll = false
                            }
                        }
                    },
                ) {
                    if (markingAll) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MatchUpColors.Lime,
                        )
                        Spacer(Modifier.width(7.dp))
                    }
                    Text(
                        text = strings.markAllRead,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (online) MatchUpColors.Lime else MatchUpColors.TextFaint,
                    )
                }
            }
        }
        Column(Modifier.padding(horizontal = 18.dp)) {
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
            if (error != null && notifications.isNotEmpty()) {
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
                loading && notifications.isEmpty() -> LoadingState()
                error != null && notifications.isEmpty() -> ErrorState(
                    title = if (!online) strings.noConnectionTitle else strings.errorGeneric,
                    body = if (!online) strings.noConnectionBody else error.orEmpty(),
                    onRetry = { refreshKey += 1 },
                )
                notifications.isEmpty() -> EmptyState(
                    title = strings.noNotifications,
                    body = strings.noNotificationsBody,
                    icon = Icons.Outlined.Notifications,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                val booking = bookingId(notification)
                                if (!notification.read && online) {
                                    scope.launch {
                                        runCatching {
                                            state.dependencies.notifications.markRead(notification.id)
                                        }.onSuccess {
                                            notifications = notifications.map {
                                                if (it.id == notification.id) it.copy(read = true) else it
                                            }
                                            unreadCount = (unreadCount - 1).coerceAtLeast(0)
                                            state.pollUnread()
                                        }.onFailure { state.showError(it) }
                                    }
                                }
                                if (booking != null) onOpenBooking(booking)
                            },
                            modifier = Modifier.padding(vertical = 5.dp),
                        )
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val hasAction = notification.payload?.get("booking_id") != null ||
        notification.payload?.get("bookingId") != null
    val icon: ImageVector = when {
        notification.type.contains("confirm") -> Icons.Outlined.CheckCircle
        notification.type.contains("cancel") -> Icons.Outlined.Cancel
        notification.type.contains("payment") -> Icons.Outlined.HourglassTop
        notification.type.contains("remind") -> Icons.Outlined.CalendarMonth
        else -> Icons.Outlined.Notifications
    }
    val tint = when {
        notification.type.contains("confirm") -> MatchUpColors.Mint
        notification.type.contains("cancel") -> MatchUpColors.Red
        notification.type.contains("payment") -> MatchUpColors.Amber
        else -> MatchUpColors.Lime
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (notification.read) MatchUpColors.Surface else MatchUpColors.SurfaceHigh,
        border = BorderStroke(
            1.dp,
            if (notification.read) MatchUpColors.OutlineSoft else MatchUpColors.Lime.copy(alpha = 0.18f),
        ),
    ) {
        Row(
            Modifier.padding(13.dp),
            verticalAlignment = Alignment.Top,
        ) {
            IconBubble(icon = icon, tint = tint, size = 40.dp)
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title.ifBlank { strings.notificationFallback },
                        style = MaterialTheme.typography.titleSmall,
                        color = MatchUpColors.Text,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!notification.read) {
                        Spacer(Modifier.width(7.dp))
                        Box(
                            Modifier
                                .size(7.dp)
                                .background(MatchUpColors.Lime, CircleShape),
                        )
                    }
                }
                if (notification.body.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextMuted,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    text = relativeTime(notification.createdAt, strings),
                    style = MaterialTheme.typography.labelSmall,
                    color = MatchUpColors.TextFaint,
                )
            }
            if (hasAction) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MatchUpColors.TextFaint,
                    modifier = Modifier
                        .padding(top = 9.dp, start = 4.dp)
                        .size(18.dp),
                )
            }
        }
    }
}
