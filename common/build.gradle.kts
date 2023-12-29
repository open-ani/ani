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
        jvmToolchain(8)
    }

    targets.all {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }

    sourceSets {
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }
        val commonMain by getting {
            dependencies {
                api(compose.foundation)
                api(compose.ui)
//                api(compose.uiTooling)
                api(compose.material3)
                api(compose.runtime)
//                api("org.jetbrains.compose.ui:ui-text:${ComposeBuildConfig.composeVersion}")

                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.1")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
                api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.4.1")
                api("io.ktor:ktor-client-websockets:2.1.1")
                api("io.ktor:ktor-client-logging:2.1.1")
                api("net.mamoe.yamlkt:yamlkt:0.12.0")
                api("dev.dirs:directories:26")

//    implementation("org.jetbrains.exposed:exposed-core:0.39.1")
//    implementation("org.jetbrains.exposed:exposed-dao:0.39.1")
//    implementation("org.jetbrains.exposed:exposed-jdbc:0.39.1")
//    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.39.1")
//    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
//    implementation("org.xerial:sqlite-jdbc:3.39.2.0")

                api(projects.dataSources.dmhy)
                implementation("org.slf4j:slf4j-simple:2.0.3")
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
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")
                api("androidx.compose.ui:ui-tooling-preview:1.5.4")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
                implementation("androidx.compose.material3:material3:1.1.2")
                implementation("com.google.accompanist:accompanist-flowlayout:0.25.1")
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.desktop.currentOs) {
                    exclude(compose.material)
                }
//                api(compose.preview)
                api(compose.material3)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3")
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
    languageSettings.optIn("androidx.compose.animation.ExperimentalAnimationApi")
    languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
    languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
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
            projects.animationGardenAndroid.dependencyProject.projectDir.resolve("proguard-rules.pro")
                .also {
                    check(it.exists()) { "Could not find ${it.absolutePath}" }
                }
        )
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6-dev-k2.0.0-Beta1-06a03be2b42"
    }
}
dependencies {
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
}
