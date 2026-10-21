package et.matchup.app.platform

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import et.matchup.app.config.GeneratedConfig
import kotlinx.coroutines.launch

private var applicationContext: Context? = null
private var launchUrl: String? = null

actual fun initializePlatform(context: Any?, initialUrl: String?) {
    applicationContext = (context as? Context)?.applicationContext
    launchUrl = initialUrl
}

actual fun platformStore(): KeyValueStore {
    val context = applicationContext ?: error("initializePlatform must be called first")
    return AndroidKeyValueStore(context.getSharedPreferences("matchup", Context.MODE_PRIVATE))
}

actual fun apiBaseUrl(): String =
    GeneratedConfig.API_BASE_URL_ANDROID.trimEnd('/').ifBlank { "http://10.0.2.2:8082/v1" }

actual fun isOnlineNow(): Boolean {
    val context = applicationContext ?: return true
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

actual fun initialLink(): String? = launchUrl

actual fun consumeInitialLink() {
    launchUrl = null
}

actual val isWebPlatform: Boolean = false

actual fun prefersReducedMotion(): Boolean {
    val context = applicationContext ?: return false
    val scale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return scale == 0f
}

actual fun hapticTick() {
    val context = applicationContext ?: return
    val vibrator = context.getSystemService(Vibrator::class.java) ?: return
    if (!vibrator.hasVibrator()) return
    vibrator.vibrate(VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE))
}

actual fun shareText(title: String, text: String) {
    val context = applicationContext ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

actual fun openMapSearch(query: String) {
    val context = applicationContext ?: return
    if (query.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }.onFailure {
        runCatching {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}

actual fun addCalendarEvent(
    title: String,
    description: String,
    location: String,
    startMillis: Long,
    endMillis: Long,
) {
    val context = applicationContext ?: return
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberGoogleSignIn(
    onToken: (String) -> Unit,
    onError: (GoogleSignInProblem) -> Unit,
): GoogleSignInHandle {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val latestToken = rememberUpdatedState(onToken)
    val latestError = rememberUpdatedState(onError)
    val clientId = GeneratedConfig.GOOGLE_WEB_CLIENT_ID.trim()
    return remember(activity, clientId) {
        GoogleSignInHandle(
            configured = clientId.isNotBlank(),
            launch = {
                if (clientId.isBlank()) {
                    latestError.value(GoogleSignInProblem.NotConfigured)
                } else if (activity == null) {
                    latestError.value(GoogleSignInProblem.Unavailable)
                } else {
                    scope.launch {
                        try {
                            val manager = CredentialManager.create(activity)
                            val option = GetGoogleIdOption.Builder()
                                .setServerClientId(clientId)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(option)
                                .build()
                            val result = manager.getCredential(activity, request)
                            val credential = result.credential
                            if (
                                credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                latestToken.value(googleCredential.idToken)
                            } else {
                                latestError.value(GoogleSignInProblem.NoAccount)
                            }
                        } catch (error: GetCredentialException) {
                            val problem = when (error) {
                                is GetCredentialCancellationException -> GoogleSignInProblem.Cancelled
                                is NoCredentialException -> GoogleSignInProblem.NoAccount
                                is GetCredentialUnsupportedException -> GoogleSignInProblem.Unavailable
                                else -> GoogleSignInProblem.Failed
                            }
                            latestError.value(problem)
                        } catch (_: Throwable) {
                            latestError.value(GoogleSignInProblem.Failed)
                        }
                    }
                }
            },
        )
    }
}

private class AndroidKeyValueStore(
    private val prefs: android.content.SharedPreferences,
) : KeyValueStore {
    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun keys(): Set<String> = prefs.all.keys.toSet()
}

private fun Context.findActivity(): ComponentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return current as? ComponentActivity
}
