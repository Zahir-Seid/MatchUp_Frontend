// The Kotlin Multiplatform plugin downloads its own pinned Node.js and Yarn
// into the Gradle user home the first time a JS/WASM task runs, so the build
// does not depend on the machine's Node installation.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}
