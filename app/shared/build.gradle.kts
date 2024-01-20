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

import com.android.build.gradle.internal.cxx.io.writeTextIfDifferent


plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
    idea
}

extra.set("ani.jvm.target", 17)

kotlin {
    androidTarget()
    jvm("desktop")

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
    }
}

kotlin {
    sourceSets.commonMain.dependencies {
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
        api(projects.dataSources.dmhy)
        api(projects.dataSources.bangumi)
        api(projects.utils.slf4jKt)
        api(projects.utils.coroutines)
        api(projects.app.torrent)

        // Ktor
        api(libs.ktor.client.websockets)
        api(libs.ktor.client.logging)

        // Others
        api(libs.koin.core) // dependency injection
        api(libs.directories) // Data directories on all OSes
        api(libs.kamel.image) // Image loading
        api(libs.datastore.core) // Data Persistence
        api(libs.datastore.preferences.core) // Preferences
        api(libs.precompose) // Navigator
        api(libs.precompose.koin) // Navigator
        api(libs.precompose.viewmodel) // Navigator

        implementation(libs.slf4j.api)
    }

    sourceSets.androidMain.dependencies {
        api(libs.kotlinx.coroutines.android)
        api(libs.datastore)
        api(libs.datastore.preferences)
        api(libs.androidx.appcompat)
        api(libs.androidx.core.ktx)
        api(libs.koin.android)
        implementation(libs.androidx.browser)
        implementation(libs.slf4j.android)

        // Compose
        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.compose.material3)

        implementation(libs.androidx.media3.ui)
        implementation(libs.androidx.media3.exoplayer)
    }

    sourceSets.named("desktopMain").dependencies {
        api(compose.desktop.currentOs) {
            exclude(compose.material) // We use material3
        }
        api(compose.material3)
        api(projects.utils.slf4jKt)
        api(libs.kotlinx.coroutines.swing)
        runtimeOnly(libs.kotlinx.coroutines.debug)
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
    namespace = "me.him188.ani"
    compileSdk = getIntProperty("android.compile.sdk")
    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
        buildConfigField("String", "VERSION_NAME", "\"${getProperty("version.name")}\"")
        buildConfigField("String", "BANGUMI_OAUTH_CLIENT_ID", "\"$bangumiClientId\"")
        buildConfigField("String", "BANGUMI_OAUTH_CLIENT_SECRET", "\"$bangumiClientSecret\"")
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


val buildConfigDesktopDir = layout.buildDirectory.file("generated/source/buildConfigDesktop")

idea {
    module {
        generatedSourceDirs.add(buildConfigDesktopDir.get().asFile)
    }
}

kotlin.sourceSets.getByName("desktopMain") {
    kotlin.srcDirs(buildConfigDesktopDir)
}

//tasks.register("generateBuildConfigForDesktop") {
//    doLast {
//        
//    }
//}
val file = buildConfigDesktopDir.get().asFile.resolve("AniBuildConfig.kt").apply { createNewFile() }
file.writeTextIfDifferent(
    """
            package me.him188.ani.app.platform
            object AniBuildConfigDesktop : AniBuildConfig {
                override val versionName = "${project.version}"
                override val bangumiOauthClientId = "$bangumiClientId"
                override val bangumiOauthClientSecret = "$bangumiClientSecret"
                override val isDebug = false
            }
            """.trimIndent()
)