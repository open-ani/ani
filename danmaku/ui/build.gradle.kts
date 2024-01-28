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

extra.set("ani.jvm.target", 17)

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        // Workaround for MPP compose bug, don't change
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }
    }
}

kotlin {
    sourceSets.commonMain.dependencies {
//        implementation(projects.danmaku.api)
        
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.serialization.json)
        compileOnly(libs.atomicfu) // No need to include in the final build since atomicfu Gradle will optimize it out

        // Compose
        api(compose.foundation)
        api(compose.animation)
        api(compose.ui)
        api(compose.material3)
        api(compose.runtime)

        api(projects.utils.slf4jKt)
        api(projects.utils.coroutines)

        // Ktor
        api(libs.ktor.client.logging)

        // Others
        api(libs.koin.core) // dependency injection

        implementation(libs.slf4j.api)
    }

    sourceSets.androidMain.dependencies {
        api(libs.kotlinx.coroutines.android)
        api(libs.androidx.appcompat)
        api(libs.androidx.core.ktx)
        api(libs.koin.android)
        implementation(libs.slf4j.android)

        // Compose
        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.compose.material3)
    }

    sourceSets.named("desktopMain").dependencies {
        api(compose.desktop.currentOs) {
            exclude(compose.material) // We use material3
        }
        api(projects.utils.slf4jKt)
        api(libs.kotlinx.coroutines.swing)
        runtimeOnly(libs.kotlinx.coroutines.debug)

        runtimeOnly(libs.slf4j.simple)
        implementation(libs.ktor.server.cio)
        implementation(libs.ktor.server.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
    }
}


configureFlattenMppSourceSets()
kotlin.sourceSets {
    getByName("desktopMain").resources.srcDirs("androidRes/raw")
}

val bangumiClientId = getPropertyOrNull("bangumi.oauth.client.id")
val bangumiClientSecret = getPropertyOrNull("bangumi.oauth.client.secret")

if (bangumiClientId == null || bangumiClientSecret == null) {
    logger.warn("bangumi.oauth.client.id or bangumi.oauth.client.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
}

android {
    namespace = "me.him188.ani.danmaku"
    compileSdk = getIntProperty("android.compile.sdk")
    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.jetpack.compose.compiler.get()
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
}
