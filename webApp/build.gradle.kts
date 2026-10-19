import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
                // The public backend does not send CORS headers. The PWA always
                // talks to its own origin, and both the dev server and Caddy
                // proxy /v1 to the API on the same origin.
                // Common ports (8080, 8090) are often taken on developer
                // machines, so the dev server uses 9173 and README documents it.
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                    port = 9173,
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/v1"),
                            target = "http://127.0.0.1:8082",
                            changeOrigin = true,
                        ),
                    ),
                )
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.ui)
        }
    }
}
