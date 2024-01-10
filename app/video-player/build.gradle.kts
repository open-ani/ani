/*
 * Ani
 * Copyright (C) 2022-2024 Him188
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

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm("desktop")

    configureFlattenMppSourceSets()

    sourceSets {
        // Workaround for MPP compose bug, don't change
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }
    }

    sourceSets.commonMain.dependencies {
        api(projects.app.shared)
    }

    sourceSets.androidMain.dependencies {
        api(libs.androidx.media3.ui)
        api(libs.androidx.media3.exoplayer)
    }
}

android {
    namespace = "me.him188.ani.player"
    compileSdk = getIntProperty("android.compile.sdk")
//    sourceSets["main"].manifest.srcFile("AndroidManifest.xml")
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
            *sharedAndroidProguardRules(),
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
