package et.matchup.app.platform

import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.credentials.exceptions.GetCredentialException
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
                        } catch (_: GetCredentialException) {
                            latestError.value(GoogleSignInProblem.Cancelled)
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
