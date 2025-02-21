// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //id("com.android.application") version "8.8.0" apply false
    //id("org.jetbrains.kotlin.android") version "2.1.20-RC" apply false
    //id("app.cash.sqldelight") version "2.0.2" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.sqldelight.plugin) apply false
    alias(libs.plugins.compose.compiler) apply false
    //!removed google analytics!id("com.google.gms.google-services") version "4.4.0" apply false
}