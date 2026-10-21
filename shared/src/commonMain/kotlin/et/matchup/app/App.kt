package et.matchup.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.matchup.app.data.AuthState
import et.matchup.app.data.UserDto
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.BrandMark
import et.matchup.app.ui.components.Wordmark
import et.matchup.app.ui.nav.AuthScreen
import et.matchup.app.ui.nav.MainScreen
import et.matchup.app.ui.nav.rememberNavigator
import et.matchup.app.ui.screens.auth.ForgotPasswordScreen
import et.matchup.app.ui.screens.auth.LoginScreen
import et.matchup.app.ui.screens.auth.ResetPasswordScreen
import et.matchup.app.ui.screens.auth.SignupScreen
import et.matchup.app.ui.screens.bookings.BookingDetailScreen
import et.matchup.app.ui.screens.bookings.BookingsScreen
import et.matchup.app.ui.screens.bookings.PaymentScreen
import et.matchup.app.ui.screens.bookings.VerificationScreen
import et.matchup.app.ui.screens.home.GameDetailScreen
import et.matchup.app.ui.screens.home.HomeScreen
import et.matchup.app.ui.screens.home.ReviewScreen
import et.matchup.app.ui.screens.notifications.NotificationsScreen
import et.matchup.app.ui.screens.profile.ChangePasswordScreen
import et.matchup.app.ui.screens.profile.EditProfileScreen
import et.matchup.app.ui.screens.profile.EmailVerificationScreen
import et.matchup.app.ui.screens.profile.ProfileScreen
import et.matchup.app.ui.screens.profile.SavedTeammatesScreen
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.ui.theme.MatchUpTheme
import et.matchup.app.platform.PlatformBackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    val container = remember { AppContainer() }
    val state = remember { AppState(container) }
    val auth by state.auth.collectAsState()
    val online by state.online.collectAsState()
    val unread by state.unreadCount.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (state.initialAuthLink?.resetToken != null) {
            container.session.clearLocal()
        }
        state.bootstrap()
    }
    LaunchedEffect(state) {
        state.messages.collect { message -> snackbar.showSnackbar(message) }
    }
    DisposableEffect(Unit) {
        onDispose { state.close() }
    }

    MatchUpTheme {
        AppFrame {
            when (val current = auth) {
                AuthState.Loading -> SplashScreen()
                AuthState.SignedOut -> AuthFlow(state = state, online = online)
                is AuthState.SignedIn -> MainFlow(
                    state = state,
                    user = current.user,
                    online = online,
                    unread = unread,
                )
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 88.dp),
            ) { data ->
                Snackbar(
                    containerColor = MatchUpColors.SurfaceHigh,
                    contentColor = MatchUpColors.Text,
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppFrame(content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchUpColors.BackgroundAlt),
        contentAlignment = Alignment.TopCenter,
    ) {
        val wide = maxWidth > 700.dp
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MatchUpColors.Lime.copy(alpha = 0.055f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.08f),
                    radius = size.width * 0.55f,
                ),
                radius = size.width * 0.55f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.08f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .background(MatchUpColors.Background)
                .imePadding()
                .then(
                    if (wide) Modifier.border(1.dp, MatchUpColors.OutlineSoft) else Modifier,
                ),
        ) {
            content()
        }
    }
}

@Composable
private fun SplashScreen() {
    val strings = LocalStrings.current
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MatchUpColors.Background, MatchUpColors.BackgroundAlt),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark(size = 76.dp)
            Spacer(Modifier.height(18.dp))
            Wordmark(markSize = 28.dp, showMark = false)
            Spacer(Modifier.height(10.dp))
            Text(
                text = strings.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MatchUpColors.TextMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(34.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MatchUpColors.Lime,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun AuthFlow(state: AppState, online: Boolean) {
    val strings = LocalStrings.current
    val initialToken = state.initialAuthLink?.resetToken
    val nav = rememberNavigator<AuthScreen>(
        if (initialToken != null) AuthScreen.Reset(initialToken) else AuthScreen.Login,
    )
    PlatformBackHandler(enabled = nav.stack.size > 1) { nav.pop() }

    Crossfade(
        targetState = nav.current,
        animationSpec = tween(180),
        label = "auth",
    ) { screen ->
        when (screen) {
            AuthScreen.Login -> LoginScreen(
                state = state,
                online = online,
                onSignup = { nav.push(AuthScreen.Signup) },
                onForgot = { nav.push(AuthScreen.Forgot) },
            )
            AuthScreen.Signup -> SignupScreen(
                state = state,
                online = online,
                onBack = { nav.pop() },
                onSignIn = { nav.replaceRoot(AuthScreen.Login) },
            )
            AuthScreen.Forgot -> ForgotPasswordScreen(
                state = state,
                online = online,
                onBack = { nav.pop() },
                onResetToken = { token -> nav.replace(AuthScreen.Reset(token)) },
            )
            is AuthScreen.Reset -> ResetPasswordScreen(
                state = state,
                online = online,
                token = screen.token,
                onBack = { nav.replaceRoot(AuthScreen.Login) },
                onDone = { nav.replaceRoot(AuthScreen.Login) },
            )
        }
    }
}

@Composable
private fun MainFlow(
    state: AppState,
    user: UserDto,
    online: Boolean,
    unread: Int,
) {
    val nav = rememberNavigator<MainScreen>(MainScreen.Home)
    val current = nav.current
    val isRoot = current is MainScreen.Home ||
        current is MainScreen.Bookings ||
        current is MainScreen.Notifications ||
        current is MainScreen.Profile

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            state.pollUnread()
        }
    }
    LaunchedEffect(Unit) {
        if (!state.initialAuthLink?.verificationToken.isNullOrBlank()) {
            nav.push(MainScreen.EmailVerification)
        }
    }
    PlatformBackHandler(enabled = nav.stack.size > 1 || current != MainScreen.Home) {
        if (!nav.pop() && current != MainScreen.Home) {
            nav.replaceRoot(MainScreen.Home)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isRoot) {
                MainBottomBar(
                    current = current,
                    unread = unread,
                    onSelect = { screen -> nav.replaceRoot(screen) },
                )
            }
        },
    ) { padding ->
        Crossfade(
            targetState = current,
            modifier = Modifier.padding(padding),
            animationSpec = tween(180),
            label = "main",
        ) { screen ->
            when (screen) {
                MainScreen.Home -> HomeScreen(
                    state = state,
                    user = user,
                    online = online,
                    unread = unread,
                    onOpenGame = { nav.push(MainScreen.GameDetail(it)) },
                    onOpenNotifications = { nav.replaceRoot(MainScreen.Notifications) },
                    onOpenProfile = { nav.replaceRoot(MainScreen.Profile) },
                )
                MainScreen.Bookings -> BookingsScreen(
                    state = state,
                    online = online,
                    onOpenBooking = { nav.push(MainScreen.BookingDetail(it)) },
                    onPay = { nav.push(MainScreen.Payment(it)) },
                )
                MainScreen.Notifications -> NotificationsScreen(
                    state = state,
                    online = online,
                    onOpenBooking = { nav.push(MainScreen.BookingDetail(it)) },
                )
                MainScreen.Profile -> ProfileScreen(
                    state = state,
                    user = user,
                    online = online,
                    onEditProfile = { nav.push(MainScreen.EditProfile) },
                    onSavedTeammates = { nav.push(MainScreen.SavedTeammates) },
                    onChangePassword = { nav.push(MainScreen.ChangePassword) },
                    onVerifyEmail = { nav.push(MainScreen.EmailVerification) },
                    onOpenNotifications = { nav.replaceRoot(MainScreen.Notifications) },
                )
                is MainScreen.GameDetail -> GameDetailScreen(
                    state = state,
                    gameId = screen.gameId,
                    online = online,
                    onBack = { nav.pop() },
                    onReview = { bookingType, spots, names ->
                        nav.push(
                            MainScreen.Review(
                                gameId = screen.gameId,
                                bookingType = bookingType,
                                spots = spots,
                                names = names,
                            ),
                        )
                    },
                )
                is MainScreen.Review -> ReviewScreen(
                    state = state,
                    gameId = screen.gameId,
                    bookingType = screen.bookingType,
                    spots = screen.spots,
                    names = screen.names,
                    online = online,
                    onBack = { nav.pop() },
                    onReserved = { bookingId -> nav.replace(MainScreen.Verification(bookingId)) },
                )
                is MainScreen.Verification -> VerificationScreen(
                    state = state,
                    bookingId = screen.bookingId,
                    online = online,
                    onBack = { nav.pop() },
                    onViewBooking = { nav.replace(MainScreen.BookingDetail(screen.bookingId)) },
                    onBrowseGames = { nav.replaceRoot(MainScreen.Home) },
                    onRetryPayment = { nav.replace(MainScreen.Payment(screen.bookingId)) },
                )
                is MainScreen.Payment -> PaymentScreen(
                    state = state,
                    bookingId = screen.bookingId,
                    online = online,
                    onBack = { nav.pop() },
                    onSubmitted = { bookingId -> nav.replace(MainScreen.Verification(bookingId)) },
                    onBookAgain = { gameId ->
                        nav.popToRoot()
                        if (gameId != null) nav.push(MainScreen.GameDetail(gameId))
                    },
                )
                is MainScreen.BookingDetail -> BookingDetailScreen(
                    state = state,
                    bookingId = screen.bookingId,
                    online = online,
                    onBack = { nav.pop() },
                    onPay = { nav.push(MainScreen.Payment(it)) },
                    onBookAgain = { gameId ->
                        if (gameId != null) {
                            nav.replace(MainScreen.GameDetail(gameId))
                        } else {
                            nav.popToRoot()
                        }
                    },
                )
                MainScreen.EditProfile -> EditProfileScreen(
                    state = state,
                    user = user,
                    online = online,
                    onBack = { nav.pop() },
                )
                MainScreen.SavedTeammates -> SavedTeammatesScreen(
                    state = state,
                    user = user,
                    online = online,
                    onBack = { nav.pop() },
                )
                MainScreen.ChangePassword -> ChangePasswordScreen(
                    state = state,
                    user = user,
                    online = online,
                    onBack = { nav.pop() },
                )
                MainScreen.EmailVerification -> EmailVerificationScreen(
                    state = state,
                    user = user,
                    online = online,
                    onBack = { nav.pop() },
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    current: MainScreen,
    unread: Int,
    onSelect: (MainScreen) -> Unit,
) {
    val strings = LocalStrings.current
    val items = listOf(
        Triple(MainScreen.Home, strings.discover, Icons.Outlined.Home),
        Triple(MainScreen.Bookings, strings.bookingsTab, Icons.Outlined.CalendarMonth),
        Triple(MainScreen.Notifications, strings.notifications, Icons.Outlined.Notifications),
        Triple(MainScreen.Profile, strings.profile, Icons.Outlined.Person),
    )
    Surface(
        color = MatchUpColors.Surface,
        shadowElevation = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            items.forEach { (screen, label, icon) ->
                val selected = current::class == screen::class
                BottomTab(
                    label = label,
                    icon = icon,
                    selected = selected,
                    badge = if (screen is MainScreen.Notifications) unread else 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(screen) },
                )
            }
        }
    }
}

@Composable
private fun BottomTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badge: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MatchUpColors.SurfaceHigh else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) MatchUpColors.Lime else MatchUpColors.TextFaint,
                    modifier = Modifier.size(22.dp),
                )
                if (badge > 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 0.dp)
                            .size(14.dp)
                            .background(MatchUpColors.Lime, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (badge > 9) "9+" else badge.toString(),
                            color = MatchUpColors.OnLime,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 8.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.1.sp,
                ),
                color = if (selected) MatchUpColors.Lime else MatchUpColors.TextFaint,
                maxLines = 1,
            )
        }
    }
}
