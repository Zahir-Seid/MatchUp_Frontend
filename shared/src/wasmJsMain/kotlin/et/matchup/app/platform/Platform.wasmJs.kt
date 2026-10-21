package et.matchup.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import et.matchup.app.config.GeneratedConfig
import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop

private object WebStore : KeyValueStore {
    override fun getString(key: String): String? =
        runCatching { window.localStorage.getItem(key) }.getOrNull()

    override fun putString(key: String, value: String) {
        runCatching { window.localStorage.setItem(key, value) }
    }

    override fun remove(key: String) {
        runCatching { window.localStorage.removeItem(key) }
    }

    override fun keys(): Set<String> = runCatching {
        val storage = window.localStorage
        (0 until storage.length).mapNotNull { index -> storage.key(index) }.toSet()
    }.getOrDefault(emptySet())
}

actual fun initializePlatform(context: Any?, initialUrl: String?) = Unit

actual fun platformStore(): KeyValueStore = WebStore

actual fun apiBaseUrl(): String {
    val configured = GeneratedConfig.API_BASE_URL_WEB.trim().ifBlank { "/v1" }
    if (configured.startsWith("http://") || configured.startsWith("https://")) {
        return configured.trimEnd('/')
    }
    val path = if (configured.startsWith("/")) configured else "/$configured"
    return window.location.origin.trimEnd('/') + path.trimEnd('/')
}

actual fun isOnlineNow(): Boolean = browserOnline()

actual fun initialLink(): String? = window.location.href

@OptIn(ExperimentalWasmJsInterop::class)
actual fun consumeInitialLink() {
    // Drop reset/verification tokens from the address bar so they are not
    // left behind in browser history.
    runCatching {
        window.history.replaceState(null, "", window.location.origin + window.location.pathname)
    }
}

actual val isWebPlatform: Boolean = true

actual fun prefersReducedMotion(): Boolean = reducedMotionQuery()

actual fun hapticTick() {
    vibrateTick()
}

actual fun shareText(title: String, text: String) {
    shareViaBrowser(title, text)
}

actual fun openMapSearch(query: String) {
    if (query.isBlank()) return
    openMapQuery(query)
}

actual fun addCalendarEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Long,
    endMillis: Long,
) {
    downloadCalendarEvent(title, description, location, startMillis.toDouble(), endMillis.toDouble())
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

@Composable
actual fun rememberGoogleSignIn(
    onToken: (String) -> Unit,
    onError: (GoogleSignInProblem) -> Unit,
): GoogleSignInHandle {
    val latestToken = rememberUpdatedState(onToken)
    val latestError = rememberUpdatedState(onError)
    val clientId = GeneratedConfig.GOOGLE_WEB_CLIENT_ID.trim()
    return remember(clientId) {
        GoogleSignInHandle(
            configured = clientId.isNotBlank(),
            launch = {
                if (clientId.isBlank()) {
                    latestError.value(GoogleSignInProblem.NotConfigured)
                } else {
                    requestGoogleSignIn(
                        clientId = clientId,
                        onToken = { token -> latestToken.value(token) },
                        onError = { code ->
                            latestError.value(
                                when (code) {
                                    "cancelled" -> GoogleSignInProblem.Cancelled
                                    "unavailable" -> GoogleSignInProblem.Unavailable
                                    else -> GoogleSignInProblem.Failed
                                },
                            )
                        },
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => navigator.onLine === true")
private external fun browserOnline(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (clientId, onToken, onError) => {
      let attempts = 0;
      const start = () => {
        const google = window.google;
        if (!google || !google.accounts || !google.accounts.id) {
          attempts += 1;
          if (attempts > 60) {
            onError('failed');
            return;
          }
          setTimeout(start, 250);
          return;
        }
        google.accounts.id.initialize({
          client_id: clientId,
          auto_select: false,
          cancel_on_tap_outside: true,
          callback: (response) => {
            if (response && response.credential) {
              onToken(response.credential);
            } else {
              onError('cancelled');
            }
          }
        });
        google.accounts.id.prompt((notification) => {
          if (notification.isNotDisplayed && notification.isNotDisplayed()) {
            onError('unavailable');
          } else if (notification.isSkippedMoment && notification.isSkippedMoment()) {
            onError('cancelled');
          }
        });
      };
      start();
    }
    """,
)
private external fun requestGoogleSignIn(
    clientId: String,
    onToken: (String) -> Unit,
    onError: (String) -> Unit,
): Unit

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches")
private external fun reducedMotionQuery(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { try { if (navigator.vibrate) navigator.vibrate(14); } catch (e) {} }")
private external fun vibrateTick(): Unit

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (title, text) => {
      try {
        if (navigator.share) {
          navigator.share({ title: title, text: text }).catch(() => {});
        } else if (navigator.clipboard) {
          navigator.clipboard.writeText(text);
        }
      } catch (e) {}
    }
    """,
)
private external fun shareViaBrowser(title: String, text: String): Unit

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (query) => {
      window.open('https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(query), '_blank');
    }
    """,
)
private external fun openMapQuery(query: String): Unit

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (title, description, location, start, end) => {
      const stamp = (ms) => new Date(ms).toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';
      const escapeText = (value) => String(value || '')
        .replace(/\\/g, '\\\\')
        .replace(/;/g, '\\;')
        .replace(/,/g, '\\,')
        .replace(/\n/g, '\\n');
      const ics = [
        'BEGIN:VCALENDAR',
        'VERSION:2.0',
        'PRODID:-//MatchUp//EN',
        'BEGIN:VEVENT',
        'UID:' + Date.now() + '@matchup.et',
        'DTSTAMP:' + stamp(Date.now()),
        'DTSTART:' + stamp(start),
        'DTEND:' + stamp(end),
        'SUMMARY:' + escapeText(title),
        'DESCRIPTION:' + escapeText(description),
        'LOCATION:' + escapeText(location),
        'END:VEVENT',
        'END:VCALENDAR'
      ].join('\r\n');
      const blob = new Blob([ics], { type: 'text/calendar;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'matchup-game.ics';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setTimeout(() => URL.revokeObjectURL(url), 4000);
    }
    """,
)
private external fun downloadCalendarEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Double,
    endMillis: Double,
): Unit
