import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    if (OperatingSystem.current().isMacOsX) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }
    
    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val androidMain by getting
        val androidUnitTest by getting
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        
        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
        }
        
        if (OperatingSystem.current().isMacOsX) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting

            iosX64Main.dependencies {
                implementation(libs.sqldelight.native.driver)
            }

            iosArm64Main.dependencies {
                implementation(libs.sqldelight.native.driver)
            }

            iosSimulatorArm64Main.dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
    }
}

android {
    namespace = "com.maincharacter.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

sqldelight {
    databases {
        create("MainCharacterDatabase") {
            packageName.set("com.maincharacter.shared.database")
        }
    }
}
