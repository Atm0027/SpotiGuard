plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val codigoVersion: Int =
    providers.gradleProperty("spotiguardVersionCode").orNull?.toIntOrNull()
        ?: System.getenv("SPOTIGUARD_VERSION_CODE")?.toIntOrNull()
        ?: 20

val nombreVersion: String =
    providers.gradleProperty("spotiguardVersionName").orNull
        ?: System.getenv("SPOTIGUARD_VERSION_NAME")
        ?: "1.0.8"

android {
    namespace = "com.spotiskip.guardian"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.audioguard.companion"
        minSdk = 26
        targetSdk = 34
        versionCode = codigoVersion
        versionName = nombreVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/release.keystore")
            storePassword = "spotiguard2026"
            keyAlias = "spotiguard"
            keyPassword = "spotiguard2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*.version"
        }
    }

    applicationVariants.all {
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "SpotiGuard-${nombreVersion}-${codigoVersion}.apk"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Shizuku API para cierre forzoso silencioso e instantáneo idéntico a PC
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
