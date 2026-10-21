package et.matchup.app.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import et.matchup.app.App
import et.matchup.app.platform.initializePlatform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // SystemBarStyle.auto follows the activity's night-mode configuration,
        // so status/navigation icon contrast flips with the light/dark theme.
        enableEdgeToEdge()
        initializePlatform(applicationContext, intent?.dataString)
        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val link = intent.dataString ?: return
        // Auth links (password reset / email verification) are applied at
        // startup. Recreate the activity so the new link is parsed by AppState.
        initializePlatform(applicationContext, link)
        recreate()
    }
}
