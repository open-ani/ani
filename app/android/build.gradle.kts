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
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    id("org.jetbrains.kotlinx.atomicfu")
    id("kotlin-parcelize")
    idea
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.app.shared.application)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.viewbinding)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

//    implementation(libs.log4j.core)
//    implementation(libs.log4j.slf4j.impl)

    implementation(libs.ktor.client.core)
}

val archs = buildList {
    val abis = getLocalProperty("ani.android.abis")
    if (abis != null) {
        addAll(abis.split(",").map { it.trim() })
    } else {
        add("arm64-v8a")
        add("armeabi-v7a")
        add("x86_64")
    }
}

android {
    namespace = "me.him188.ani.android"
    compileSdk = getIntProperty("android.compile.sdk")
    defaultConfig {
        applicationId = "me.him188.ani"
        minSdk = getIntProperty("android.min.sdk")
        targetSdk = getIntProperty("android.compile.sdk")
        versionCode = getIntProperty("android.version.code")
        versionName = project.version.toString()
        ndk {
            // Specifies the ABI configurations of your native
            // libraries Gradle should build and package with your app.
            abiFilters.clear()
            //noinspection ChromeOsAbiSupport
            abiFilters += archs
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            //noinspection ChromeOsAbiSupport
            include(*archs.toTypedArray())
            isUniversalApk = true // 额外构建一个
        }
    }
    signingConfigs {
        kotlin.runCatching { getProperty("signing_release_storeFileFromRoot") }.getOrNull()?.let {
            create("release") {
                storeFile = rootProject.file(it)
                storePassword = getProperty("signing_release_storePassword")
                keyAlias = getProperty("signing_release_keyAlias")
                keyPassword = getProperty("signing_release_keyPassword")
            }
        }
        kotlin.runCatching { getProperty("signing_release_storeFile") }.getOrNull()?.let {
            create("release") {
                storeFile = file(it)
                storePassword = getProperty("signing_release_storePassword")
                keyAlias = getProperty("signing_release_keyAlias")
                keyPassword = getProperty("signing_release_keyPassword")
            }
        }
    }
    packaging {
        resources {
            merges.add("META-INF/DEPENDENCIES") // log4j
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                *sharedAndroidProguardRules(),
            )
        }
        debug {
            applicationIdSuffix = ".debug2"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    externalNativeBuild {
        cmake {
            path = projects.torrent.anitorrent.anitorrentNative.dependencyProject.projectDir.resolve("CMakeLists.txt")
        }
    }

}

idea {
    module {
        excludeDirs.add(file(".cxx"))
    }
}
