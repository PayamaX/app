import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.sqldelight.plugin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.openapi.generator)
}

sqldelight {
    databases {
        register("PayamaxDatabase") {
            packageName.set("no1.payamax.db")
            generateAsync = true
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
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    //noinspection GradleDependency
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    testImplementation(libs.sqldelight.sqlite.driver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
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

    implementation(libs.androidx.work.runtime.ktx)
}



val apiSpec = layout.projectDirectory.file("api/public.json")
val apiClientDir = layout.buildDirectory.dir("generated/api")

val apiGet by tasks.registering {
    group = "api"
    description = "Downloads the public OpenAPI document to api/public.json."
    outputs.upToDateWhen { false }

    val apiUrl = providers.environmentVariable("PAX_API_URL")
    val specFile = apiSpec
    doLast {
        val base = apiUrl.orNull?.trim()?.trimEnd('/')
            ?: throw GradleException("PAX_API_URL environment variable is not set")
        val specUrl = "$base/v3/api-docs/public"
        val destination = specFile.asFile
        destination.parentFile.mkdirs()

        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
        val request = HttpRequest.newBuilder(URI.create(specUrl)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299 || response.body().isBlank()) {
            throw GradleException("Failed to download OpenAPI document from $specUrl (${response.statusCode()})")
        }

        destination.writeText(response.body())
        logger.lifecycle("Downloaded OpenAPI document to ${destination.path}")
    }
}

val apiGen by tasks.registering(GenerateTask::class) {
    group = "api"
    description = "Generates the Kotlin API client from api/public.json."
    generatorName.set("kotlin")
    inputSpec.set(apiSpec.asFile.absolutePath)
    outputDir.set(apiClientDir)
    packageName.set("no1.payamax.api")
    apiPackage.set("no1.payamax.api.apis")
    modelPackage.set("no1.payamax.api.models")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "serializationLibrary" to "gson",
        )
    )
    globalProperties.set(
        mapOf(
            "apiDocs" to "false",
            "modelDocs" to "false",
        )
    )
    cleanupOutput.set(true)
    mustRunAfter(apiGet)
}

tasks.register("apiGent") {
    group = "api"
    description = "Downloads the public OpenAPI document and generates the API client."
    dependsOn(apiGet, apiGen)
}

android.sourceSets.named("main") {
    kotlin.srcDir(apiClientDir.map { it.dir("src/main/kotlin") })
}

tasks.named("preBuild").configure {
    dependsOn(apiGen)
}