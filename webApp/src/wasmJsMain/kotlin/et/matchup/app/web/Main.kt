package et.matchup.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import et.matchup.app.platform.initializePlatform
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializePlatform(context = null, initialUrl = window.location.href)
    ComposeViewport(viewportContainerId = "webApp") {
        App()
    }
}
