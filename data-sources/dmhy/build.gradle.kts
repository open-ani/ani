/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
}

kotlin {
    sourceSets.all {
        languageSettings.progressiveMode = true
        languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
    }

    androidTarget()
    jvm {
        jvmToolchain(8)
    }

    targets.all {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                val ktorVersion = "2.3.6"
                api("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.4.1")
                // https://mvnrepository.com/artifact/org.jsoup/jsoup
                implementation("org.jsoup:jsoup:1.15.2")
                // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
                implementation("org.slf4j:slf4j-api:2.0.7")

                api(projects.protocol)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3")

                implementation(kotlin("test-junit5"))
                // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
                // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
            }
        }
    }
}

android {
    namespace = "me.him188.animationgarden"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes.getByName("release") {
        isMinifyEnabled = true
        isShrinkResources = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            projects.app.android.dependencyProject.projectDir.resolve("proguard-rules.pro").also {
                check(it.exists()) { "Could not find ${it.absolutePath}" }
            }
        )
    }
}

//tasks.test {
//    useJUnitPlatform()
//}