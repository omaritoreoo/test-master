plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    // Pastikan versi plugin serialization sesuai dengan versi Kotlin Anda
    kotlin("plugin.serialization") version "1.9.0" // Menggunakan versi Kotlin 1.9.0
}

android {
    namespace = "com.example.test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        // Pastikan ini cocok dengan versi Kotlin Anda (Kotlin 1.9.0 -> Compose Compiler 1.5.11)
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    // Jetpack Compose Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Penting untuk konsistensi versi Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Versi diambil dari BOM (misal: 1.2.1)
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel + LiveData + Lifecycle (diperbarui ke versi stabil terbaru)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.7") // Update ke versi terbaru stabil

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room (Database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1") // Menggunakan KSP untuk Room compiler

    // Accompanist (Flow Layout, etc)
    implementation("com.google.accompanist:accompanist-flowlayout:0.30.1")

    // Coil (Image loading)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlin Coroutines (diperbarui ke versi stabil terbaru)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // --- Retrofit dan GSON (untuk serialisasi JSON) ---
    // Retrofit Core
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit Converter untuk GSON (karena Anda menggunakan @SerializedName dari Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // <-- Pastikan ini, bukan Moshi atau Kotlinx Serialization converter
    // OkHttp Logging Interceptor (untuk debugging network calls)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.room:room-testing:2.6.1")// Atau versi terbaru yang stabil

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp dan Logging Interceptor (sudah ada)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}