import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val apiKeysFile = rootProject.file("app/api_keys.properties")
val apiKeys = Properties().apply {
    if (apiKeysFile.exists()) load(apiKeysFile.inputStream())
}

android {
    namespace = "com.xilingyuli.weather"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.xilingyuli.weather"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "WCN_API_KEY", "\"${apiKeys.getProperty("WCN_API_KEY", "")}\"")
        buildConfigField("String", "QW_API_HOST", "\"${apiKeys.getProperty("QW_API_HOST", "")}\"")
        buildConfigField("String", "QW_API_KEY", "\"${apiKeys.getProperty("QW_API_KEY", "")}\"")
        buildConfigField("String", "CY_TOKEN", "\"${apiKeys.getProperty("CY_TOKEN", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON
    implementation("com.google.code.gson:gson:2.11.0")

    // ViewModel (provides viewModelScope)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.3")

    // Widget - using RemoteViews directly (no Glance needed)
}
