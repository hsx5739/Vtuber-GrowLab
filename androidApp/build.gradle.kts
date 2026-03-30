plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localConfig(name: String, fallback: String = ""): String {
    return (localProperties.getProperty(name) ?: System.getenv(name) ?: fallback)
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

fun localValue(name: String, fallback: String = ""): String {
    return localProperties.getProperty(name) ?: System.getenv(name) ?: fallback
}

android {
    namespace = "com.maincharacter.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maincharacter.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "OPENAI_API_KEY", "\"${localConfig("OPENAI_API_KEY")}\"")
        buildConfigField("String", "OPENAI_MODEL", "\"${localConfig("OPENAI_MODEL", "gpt-4.1-mini")}\"")
        buildConfigField("String", "OPENAI_BASE_URL", "\"${localConfig("OPENAI_BASE_URL", "https://api.openai.com")}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(localValue("RELEASE_STORE_FILE"))
            storePassword = localValue("RELEASE_STORE_PASSWORD")
            keyAlias = localValue("RELEASE_KEY_ALIAS")
            keyPassword = localValue("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
