// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9 compiles Kotlin itself. This classpath entry selects a newer compiler than the one AGP bundles.
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.sqldelight.plugin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.openapi.generator) apply false
}