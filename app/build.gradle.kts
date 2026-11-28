import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.gradle.internal.tasks.FinalizeBundleTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.sqldelight.plugin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

sqldelight {
    databases {
        register("PayamaxDatabase") {
            packageName.set("no1.payamax.db")
            generateAsync = true
        }
    }
}

fun javaStringLiteral(value: String): String {
    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"" + escaped + "\""
}

fun envar(name: String): String {
    return providers.environmentVariable(name).orNull?.trim()?.takeIf { it.isNotEmpty() }
        ?: throw GradleException("$name environment variable is not set")
}

android {
    namespace = "no1.payamax"
    compileSdk = 37

    defaultConfig {
        applicationId = "no1.payamax"
        minSdk = 26
        targetSdk = 37
        versionCode = 19
        versionName = "$versionCode"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "PAX_API_URL", javaStringLiteral(envar("PAX_API_URL")))
        buildConfigField("String", "PAX_AUTH_REALM_URL", javaStringLiteral(envar("PAX_AUTH_REALM_URL")))
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            buildConfigField("String", "PAX_API_URL", """"https://api.pax.no1.ir:1443"""")
            buildConfigField("String", "PAX_AUTH_REALM_URL", """"https://auth.pax.no1.ir:1443/realms/pax"""")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
			ndk {
				debugSymbolLevel = "FULL"
			}
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val signedArtifactStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm", Locale.US))
val signedArtifactSeparator = "\uFF3F"

fun ensureDebugKeystore(storeFile: File, storePassword: String, keyAlias: String, keyPassword: String) {
    if (storeFile.isFile) return
    storeFile.parentFile?.mkdirs()
    val keytoolName = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) "keytool.exe" else "keytool"
    val keytool = File(System.getProperty("java.home"), "bin/$keytoolName")
    val execution = providers.exec {
        commandLine(
            keytool.absolutePath,
            "-genkeypair",
            "-keystore", storeFile.absolutePath,
            "-storetype", "PKCS12",
            "-storepass", storePassword,
            "-alias", keyAlias,
            "-keypass", keyPassword,
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-dname", "CN=Android Debug,O=Android,C=US",
        )
        isIgnoreExitValue = true
    }
    val completed = execution.result.get()
    if (completed.exitValue != 0 || !storeFile.isFile) {
        val output = execution.standardOutput.asText.get() + execution.standardError.asText.get()
        throw GradleException(
            "Could not create debug keystore at ${storeFile.path} (exit ${completed.exitValue}): $output",
        )
    }
    logger.lifecycle("Created debug keystore at ${storeFile.path}")
}

fun certificateSha1(storeFile: File, storePassword: String, keyAlias: String, storeType: String?): String {
    val types = buildList {
        if (!storeType.isNullOrBlank()) add(storeType)
        add("PKCS12")
        add("JKS")
    }.distinct()
    var loadFailure: Exception? = null
    for (type in types) {
        try {
            val keystore = KeyStore.getInstance(type)
            storeFile.inputStream().use { keystore.load(it, storePassword.toCharArray()) }
            val aliases = buildList {
                val enumeration = keystore.aliases()
                while (enumeration.hasMoreElements()) add(enumeration.nextElement())
            }
            val alias = aliases.firstOrNull { it.equals(keyAlias, ignoreCase = true) }
                ?: throw GradleException("Alias '$keyAlias' was not found in ${storeFile.path}")
            val certificate = keystore.getCertificate(alias) as X509Certificate
            return MessageDigest.getInstance("SHA-1").digest(certificate.encoded)
                .joinToString(separator = "_") { "%02X".format(it) }
        } catch (error: GradleException) {
            throw error
        } catch (error: Exception) {
            loadFailure = error
        }
    }
    throw GradleException("Could not read the SHA-1 of '$keyAlias' from $storeFile", loadFailure)
}

fun signingCertificateSha1(buildTypeName: String): String {
    val injectedStore = providers.gradleProperty("android.injected.signing.store.file").orNull
    val injectedStorePassword = providers.gradleProperty("android.injected.signing.store.password").orNull
    val injectedAlias = providers.gradleProperty("android.injected.signing.key.alias").orNull
    if (
        !injectedStore.isNullOrBlank() &&
        !injectedStorePassword.isNullOrBlank() &&
        !injectedAlias.isNullOrBlank() &&
        !providers.gradleProperty("android.injected.signing.key.password").orNull.isNullOrBlank()
    ) {
        return certificateSha1(
            storeFile = File(injectedStore),
            storePassword = injectedStorePassword,
            keyAlias = injectedAlias,
            storeType = providers.gradleProperty("android.injected.signing.store.type").orNull,
        )
    }

    val buildType = android.buildTypes.getByName(buildTypeName)
    val signingConfig: ApkSigningConfig = buildType.signingConfig
        ?: if (buildType.isDebuggable) android.signingConfigs.getByName("debug") else null
        ?: throw GradleException("Build type '$buildTypeName' has no signing config")
    val storeFile = signingConfig.storeFile
        ?: throw GradleException("Signing config '${signingConfig.name}' has no store file")
    val storePassword = signingConfig.storePassword
        ?: throw GradleException("Signing config '${signingConfig.name}' has no store password")
    val keyAlias = signingConfig.keyAlias
        ?: throw GradleException("Signing config '${signingConfig.name}' has no key alias")
    val keyPassword = signingConfig.keyPassword ?: storePassword
    if (!storeFile.isFile && signingConfig.name == "debug") {
        ensureDebugKeystore(storeFile, storePassword, keyAlias, keyPassword)
    }
    return certificateSha1(storeFile, storePassword, keyAlias, signingConfig.storeType)
}

androidComponents {
    onVariants { variant ->
        val signingSha1 = signingCertificateSha1(variant.buildType ?: variant.name)
        val artifactBaseName = variant.applicationId.zip(variant.outputs.first().versionCode) { applicationId, versionCode ->
            listOf(applicationId, signedArtifactStamp, versionCode.toString(), signingSha1)
                .joinToString(separator = signedArtifactSeparator)
        }
        variant.outputs.forEach { output ->
            output.outputFileName.set(artifactBaseName.map { "$it.apk" })
        }

        val signBundleTask = "sign${variant.name.replaceFirstChar { it.titlecase(Locale.US) }}Bundle"
        afterEvaluate {
            tasks.named(signBundleTask, FinalizeBundleTask::class.java).configure {
                val plannedBundle = finalBundleFile.asFile.get()
                finalBundleFile.set(plannedBundle.parentFile.resolve("${artifactBaseName.get()}.aab"))
            }
        }
    }
}

dependencies {
    implementation(libs.org.xerial.sqlite.jdbc) // Use the latest version

    implementation(libs.androidx.foundation)
    implementation(libs.accompanist.permissions)

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
    implementation(libs.coil.compose)
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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}



val apiSpec = layout.projectDirectory.file("api/public.json")
val apiClientDir = layout.buildDirectory.dir("generated/api")

val apiGet = tasks.register("apiGet") {
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

val apiGen = tasks.register<GenerateTask>("apiGen") {
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

// The generator writes a full project tree. AGP needs a directory that contains only the Kotlin sources.
abstract class ApiKotlinSources : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copySources() {
        val destination = outputDir.get().asFile
        destination.deleteRecursively()
        val source = inputDir.get().asFile
        source.walkTopDown().filter { it.isFile }.forEach { file ->
            val target = destination.resolve(source.toPath().relativize(file.toPath()).toString())
            target.parentFile.mkdirs()
            if (file.extension == "kt") {
                val original = file.readText()
                val suppress = "@file:Suppress(\"warnings\")"
                val replaced = Regex("""@file:Suppress\s*\([^)]*\)""")
                    .replace(original, suppress)
                target.writeText(if (replaced == original) "$suppress\n\n$original" else replaced)
            } else {
                file.copyTo(target, overwrite = true)
            }
        }
    }
}

val apiKotlinSources = tasks.register<ApiKotlinSources>("apiKotlinSources") {
    dependsOn(apiGen)
    inputDir.set(apiClientDir.map { it.dir("src/main/kotlin") })
    outputDir.set(layout.buildDirectory.dir("generated/api-kotlin"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.kotlin?.addGeneratedSourceDirectory(apiKotlinSources, ApiKotlinSources::outputDir)
    }
}