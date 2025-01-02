plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt") // Modificado para build.gradle.kts
    id("com.google.gms.google-services") // Aplica el plugin de Google Services
    kotlin("plugin.serialization")version  "1.9.0"
}

android {
    namespace = "com.vidaensupalabra.vsp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vidaensupalabra.vsp"
        minSdk = 24
        targetSdk = 35
        versionCode = 16
        versionName = "1.4.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "APPLICATION_ID", "\"com.vidaensupalabra.vsp\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true // Agrega esta línea para habilitar BuildConfig
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "34.0.0"


}

// Añadir Room Database y Compiler

dependencies {

    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material:material:1.7.6") // Versión de Material Design 2
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("com.google.firebase:protolite-well-known-types:18.0.0")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Añadir Room Database y Compiler con la sintaxis correcta para Kotlin
    val roomVersion = "2.6.1" // Define la versión de Room aquí
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Ya lo tienes, pero es correcto para coroutines
    implementation ("com.google.firebase:firebase-firestore:25.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-messaging")
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation ("androidx.work:work-runtime-ktx:2.10.0")
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.1")
    implementation ("com.google.android.play:core:1.10.3")
    implementation ("io.coil-kt:coil-compose:2.7.0")
    implementation ("androidx.work:work-runtime-ktx:2.10.0") // Asegúrate de usar la última versión
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.json:json:20240303")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jsoup:jsoup:1.18.3") // Asegúrate de usar la última versión disponible
    // ExoPlayer y UI para reproducción de videos
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")

    // Kotlinx Serialization para JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Coil para manejar imágenes en Compose
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Recursos para testing (solo si se necesitan)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.31.3-beta")
}
