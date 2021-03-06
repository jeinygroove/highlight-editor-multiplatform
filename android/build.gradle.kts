plugins {
    id("org.jetbrains.compose") version "1.0.0-alpha4-build331"
    id("com.android.application")
    kotlin("android")
}

group = "me.olgashimanskaia"
version = "1.0"

repositories {
    jcenter()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.3.0")
    implementation(project(mapOf("path" to ":common")))
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "me.olgashimanskaia.android"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.Experimental")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    dexOptions {
        javaMaxHeapSize = "4G"
    }
}