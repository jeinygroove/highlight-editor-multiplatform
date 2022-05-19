import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0-alpha4-build331"
}

version = "1.0"
val ktorVersion = "2.0.0"
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.Experimental", "-Xallow-jvm-ir-dependencies")
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("ai.grazie.client:client-okhttp-jvm:0.2.12")
                implementation("ai.grazie.gec:gec-agg-cloud-engine-jvm:0.2.12")
                implementation("ai.grazie.nlp:nlp-langs:0.2.12")
                implementation("ai.grazie.nlp:nlp-tokenizer-jvm:0.2.12")
                implementation("ai.grazie.docs:docs-format-grazie:0.2.13")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jvm"
            packageVersion = "1.0.0"
        }
    }
}