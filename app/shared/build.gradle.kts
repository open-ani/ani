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
@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)


plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
}

kotlin.sourceSets.all {
    languageSettings.enableLanguageFeature("ContextReceivers")
}

kotlin {
    androidTarget()
    jvm("desktop") {
        jvmToolchain(17)
    }

    targets.all {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }

    sourceSets {
        // Workaround for MPP compose bug, don't change
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }

        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.serialization.json)
                compileOnly(libs.atomicfu) // No need to include in the final build since atomicfu Gradle will optimize it out

                // Compose
                api(compose.foundation)
                api(compose.animation)
                api(compose.ui)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.runtime)

                // Subprojects: data sources and utils
                api(projects.protocol)
                api(projects.dataSources.dmhy)
                api(projects.dataSources.bangumi)
                api(projects.utils.slf4jKt)

                // Ktor
                api(libs.ktor.client.websockets)
                api(libs.ktor.client.logging)

                // Others
                api(libs.koin.core) // dependency injection
                api(libs.directories) // Data directories on all OSes
                api(libs.kamel.image) // Image loading
                api(libs.datastore.preferences.core) // Preferences
                api(libs.precompose) // Navigator
                api(libs.precompose.koin) // Navigator
                api(libs.precompose.viewmodel) // Navigator

                implementation(libs.slf4j.simple)
            }
        }

        commonMain.resources.srcDir(projectDir.resolve("src/androidMain/res/raw"))

        val commonTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.android)
                api(libs.datastore.preferences)
                api(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
                api(libs.koin.android)

                // Compose
                api(libs.androidx.compose.ui.tooling.preview)
                api(libs.androidx.compose.material3)
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.desktop.currentOs) {
                    exclude(compose.material) // We use material3
                }
                api(compose.material3)
                api(projects.utils.slf4jKt)
                api(libs.kotlinx.coroutines.swing)
                runtimeOnly(libs.kotlinx.coroutines.debug)
            }
        }
    }
}

android {
    namespace = "me.him188.animationgarden"
    compileSdk = getIntProperty("android.compile.sdk")
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
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
            projects.app.android.dependencyProject.projectDir.resolve("proguard-rules.pro")
                .also {
                    check(it.exists()) { "Could not find ${it.absolutePath}" }
                }
        )
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.jetpack.compose.compiler.get()
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
}
 