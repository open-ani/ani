import Os.Linux
import Os.MacOS
import Os.Windows

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

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    jvmToolchain(17)
    explicitApi()

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
        api(libs.kotlinx.coroutines.core)
        implementation(libs.libtorrent4j)
        implementation(projects.utils.slf4jKt)
        api(projects.utils.io)
    }

    sourceSets.commonTest.dependencies {
        api(libs.kotlinx.coroutines.test)
        runtimeOnly(libs.slf4j.simple)
    }

    sourceSets.androidMain.dependencies {
        api(libs.kotlinx.coroutines.android)
        implementation(libs.libtorrent4j.android.arm64)
    }

    sourceSets.named("desktopMain").dependencies {
        api(libs.kotlinx.coroutines.swing)
        implementation(
            when (getOs()) {
                Windows -> libs.libtorrent4j.windows
                MacOS -> libs.libtorrent4j.macos
                Linux -> libs.libtorrent4j.linux
                else -> {
                    logger.warn("Unrecognized architecture, libtorrent4j will not be included")
                }
            }
        )
    }
}

android {
    namespace = "me.him188.ani.torrent"
    compileSdk = getIntProperty("android.compile.sdk")
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
}
