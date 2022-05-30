import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0-alpha4-build331"
    id("com.android.library")
    kotlin("plugin.serialization") version "1.4.21"
}

group = "me.olgashimanskaia"
version = "1.0"
val ktorVersion = "2.0.0"

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            //kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.Experimental", "-Xallow-jvm-ir-dependencies")
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.materialIconsExtended)
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.2.0")
                api("androidx.core:core-ktx:1.3.1")
                implementation("ai.grazie.client:client-okhttp-jvm:0.2.12")
                implementation("ai.grazie.gec:gec-agg-cloud-engine-jvm:0.2.12")
                implementation("ai.grazie.nlp:nlp-langs:0.2.12")
                implementation("ai.grazie.nlp:nlp-tokenizer-jvm:0.2.12")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                implementation("ai.grazie.client:client-okhttp-jvm:0.2.12")
                implementation("ai.grazie.gec:gec-agg-cloud-engine-jvm:0.2.12")
                implementation("ai.grazie.nlp:nlp-langs:0.2.12")
                implementation("ai.grazie.nlp:nlp-tokenizer-jvm:0.2.12")
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(31)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(31)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation("androidx.compose.foundation:foundation:1.0.0-beta04")
}
