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

import java.util.Properties

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    id("kotlinx-atomicfu")
    id("kotlin-parcelize")
}

dependencies {
    implementation(projects.dataSources.dmhy)
    implementation(projects.app.shared)
    implementation("androidx.activity:activity-compose:1.6.1")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.8.0")

    val composeVersion = "1.2.0"
    implementation("androidx.compose.ui:ui:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-viewbinding:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material3:material3:1.1.0-alpha05")
//    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.3")

    api("io.ktor:ktor-client-core:2.1.1")
}

android {
    namespace = "me.him188.animationgarden.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "me.him188.animationgarden"
        minSdk = 26
        targetSdk = 34
        versionCode = getProperty("version.code").toInt()
        versionName = project.version.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6-dev-k2.0.0-Beta1-06a03be2b42"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                projectDir.resolve("proguard-rules.pro").also {
                    check(it.exists()) { "Could not find ${it.absolutePath}" }
                }
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
    languageSettings.optIn("androidx.compose.animation.ExperimentalAnimationApi")
    languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
    languageSettings.enableLanguageFeature("ContextReceivers")
}

fun getProperty(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: properties[name]?.toString()
        ?: getLocalProperty(name)
        ?: ext.get(name).toString()


val Project.localPropertiesFile get() = project.rootProject.file("local.properties")

fun Project.getLocalProperty(key: String): String? {
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().buffered().use { input ->
            properties.load(input)
        }
        return properties.getProperty(key)
    } else {
        localPropertiesFile.createNewFile()
        return null
    }
}
