import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.sentry)
}

android {
    namespace = "cu.edu.inca.abonosverdes"
    compileSdk = 37

    defaultConfig {
        applicationId = "cu.edu.inca.abonosverdes"
        minSdk = 24
        targetSdk = 37
        versionCode = 10
        versionName = "1.0.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            keepDebugSymbols.add("**/libandroidx.graphics.path.so")
            keepDebugSymbols.add("**/libdatastore_shared_counter.so")
            keepDebugSymbols.add("**/libsentry-android.so")
            keepDebugSymbols.add("**/libsentry.so")
        }
    }
}

hilt {
    enableAggregatingTask = true
}

// FIX DEFINITIVO PARA MOSHI KAPT WARNING:
// Forzamos la exclusión de Moshi Codegen de todas las rutas de procesamiento que no sean KSP.
configurations.all {
    if (name.contains("kapt", ignoreCase = true) || 
        name.contains("annotationProcessor", ignoreCase = true) ||
        name.contains("hilt", ignoreCase = true)) {
        exclude(group = "com.squareup.moshi", module = "moshi-kotlin-codegen")
    }
}

// Desactiva físicamente las tareas de Kapt para evitar que se ejecuten e informen avisos.
tasks.matching { it.name.contains("kapt", ignoreCase = true) }.configureEach {
    enabled = false
}

sentry {
    // URL de tu servidor self-hosted
    url.set("https://sentry.inca.edu.cu/")
    
    // Inyectamos el token directamente desde la variable de entorno
    val token = System.getenv("SENTRY_AUTH_TOKEN")
    authToken.set(token)
    
    val hasToken = !token.isNullOrEmpty()
    
    // Solo intenta subir si hay un token, evitando que el build falle en CI
    includeProguardMapping.set(hasToken)
    autoUploadProguardMapping.set(hasToken)
    autoUploadSourceContext.set(hasToken)
    uploadNativeSymbols.set(hasToken)
    includeNativeSources.set(hasToken)
    
    tracingInstrumentation {
        // Desactivado para evitar errores de clases no resueltas en compilación
        enabled.set(false)
    }
}

// Silencia los 24 avisos de @param:Json para Kotlin 2.0+
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        if (!freeCompilerArgs.get().contains("-Xannotation-default-target=param-property")) {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.window)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.moshi)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.sentry.android)
    implementation(libs.sentry.compose)
}
