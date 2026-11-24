plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.zipline.gradle.plugin) apply false
    alias(libs.plugins.spotubeGradle) apply false
    alias(libs.plugins.vlcjBundler) apply false
    alias(libs.plugins.uniffi) apply false
    alias(libs.plugins.cargo) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.neucleusFramework) apply false
}