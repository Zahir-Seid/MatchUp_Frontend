package et.matchup.app.platform

import androidx.compose.runtime.Composable

/**
 * A tiny key/value store. Android uses private SharedPreferences; the PWA uses
 * localStorage. Call [initializePlatform] before touching it.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun keys(): Set<String>
}

/** Android passes its application context here for storage and platform services. */
expect fun initializePlatform(context: Any?, initialUrl: String?)

expect fun platformStore(): KeyValueStore

/** Always absolute. The web implementation resolves a relative configured path. */
expect fun apiBaseUrl(): String

expect fun isOnlineNow(): Boolean

/** The URL that launched the app, used for password-reset links. */
expect fun initialLink(): String?

/** Called once the app has read [initialLink], so the token stops lingering. */
expect fun consumeInitialLink()

expect val isWebPlatform: Boolean

/** True when the OS asks apps to avoid non-essential animation. */
expect fun prefersReducedMotion(): Boolean

/** Short confirmation buzz. No-ops on devices/browsers without a vibrator. */
expect fun hapticTick()

/** Platform share sheet; falls back to copying the text. */
expect fun shareText(title: String, text: String)

/** Opens a maps search for a free-text query (name + location). */
expect fun openMapSearch(query: String)

/** Opens the platform calendar "new event" flow. Times are epoch milliseconds. */
expect fun addCalendarEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Long,
    endMillis: Long,
)

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

data class GoogleSignInHandle(
    val configured: Boolean,
    val launch: () -> Unit,
)

/** Why a Google sign-in attempt did not produce an ID token. */
enum class GoogleSignInProblem {
    /** No client ID was generated into the build. */
    NotConfigured,

    /** The flow cannot run here (no Activity, script blocked, popup suppressed). */
    Unavailable,

    /** The user dismissed the flow. */
    Cancelled,

    /** Google responded without a usable ID token. */
    NoAccount,

    /** Anything else, such as a network or Play services failure. */
    Failed,
}

@Composable
expect fun rememberGoogleSignIn(
    onToken: (String) -> Unit,
    onError: (GoogleSignInProblem) -> Unit,
): GoogleSignInHandle
