plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.sqldelight.plugin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

sqldelight {
    databases {
        register("PayamaxDatabase") {
            packageName.set("no1.payamax.db")
        }
    }
}

android {
    namespace = "no1.payamax"
    compileSdk = 34

    defaultConfig {
        applicationId = "no1.payamax"
        minSdk = 26
        targetSdk = 34
        versionCode = 14
        versionName = "14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.org.xerial.sqlite.jdbc) // Use the latest version

    implementation(libs.androidx.foundation)
    implementation(libs.accompanist.permissions)
    //!removed google analytics!implementation("com.google.firebase:firebase-analytics:17.4.1")

    //noinspection GradleDependency
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    //noinspection GradleDependency
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.gson)
    implementation(libs.commons.io)

    implementation(libs.androidx.navigation.compose)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)


    //sqldelight
    implementation(libs.sqldelight.coroutines.extensions.jvm)
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.primitive.adapters)

    //koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
}