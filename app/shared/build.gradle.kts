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
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi


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

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        // Workaround for MPP compose bug, don't change
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }
    }
}

configureFlattenMppSourceSets()

compose.resources {
    packageOfResClass = "me.him188.ani.app"
    generateResClass = always
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
        implementation(compose.components.resources)

        // Data sources
        api(projects.dataSources.api)
        api(projects.dataSources.core)
        api(projects.dataSources.dmhy)
        api(projects.dataSources.acgRip)
        api(projects.dataSources.mikan)
        api(projects.dataSources.bangumi)

        // Danmaku
//        api(projects.danmaku.api)
//        api(projects.danmaku.ui)
//        api(projects.danmaku.dandanplay)

        api(projects.utils.slf4jKt)
        api(projects.utils.coroutines)
        api(projects.utils.io)
        api(projects.torrent)
//        api(projects.danmaku.api)
//        api(projects.danmaku.protocol)
//        api(projects.danmaku.dandanplay)
//        api(projects.danmaku.ani.client)
        api(projects.utils.ktorClient)

        // Ktor
        api(libs.ktor.client.websockets)
        api(libs.ktor.client.logging)
        api(libs.ktor.client.cio)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.serialization.kotlinx.json)

        // Others
        api(libs.koin.core) // dependency injection
        api(libs.directories) // Data directories on all OSes
        api(libs.coil.core) // Image loading
//        api(libs.coil.gif) // Image loading
        api(libs.coil.svg) // Image loading
        api(libs.coil.compose.core) // Image loading
        api(libs.coil.network.okhttp) // Image loading
        api(libs.datastore.core) // Data Persistence
        api(libs.datastore.preferences.core) // Preferences
        api(libs.precompose) // Navigator
        api(libs.precompose.koin) // Navigator
        api(libs.precompose.viewmodel) // Navigator

//        api(libs.okhttp)
//        api(libs.okhttp.logging)
        implementation(libs.reorderable)

        implementation(libs.slf4j.api)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlinx.coroutines.test)
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

        api(libs.coil)

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

        runtimeOnly(libs.slf4j.simple)
        implementation(libs.ktor.server.cio)
        implementation(libs.ktor.server.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
    }

    sourceSets {
        // TODO: 临时解决方案, KT-65362 Cannot resolve declarations from a dependency when there are multiple JVM-only project dependencies in a JVM-Android MPP
        //  https://youtrack.jetbrains.com/issue/KT-65362
        // Danmaku

        fun submodule(dir: String) {
            commonMain {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/src/"))
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/commonMain/"))
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/common/"))
                resources.srcDirs(rootProject.projectDir.resolve("$dir/resources/"))
            }
            commonTest {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/test/"))
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/commonTest/"))
            }
            androidMain {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/androidMain/"))
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/android/"))
            }
            getByName("desktopMain") {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/desktopMain/"))
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/desktop/"))
            }
            commonTest {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/commonTest/"))
            }
            getByName("androidUnitTest") {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/androidUnitTest/"))
            }
            getByName("desktopTest") {
                kotlin.srcDirs(rootProject.projectDir.resolve("$dir/desktopTest/"))
            }
        }

        submodule("danmaku/protocol")
        submodule("danmaku/api")
        submodule("danmaku/ani/client")
        submodule("danmaku/dandanplay")
        submodule("danmaku/ui")

        submodule("app/shared/foundation")
        submodule("app/shared/placeholder")

        submodule("app/shared/data")

        submodule("app/shared/bangumi-authentication")
        submodule("app/shared/video-player")

        submodule("app/shared/pages/profile")
        submodule("app/shared/pages/subject-collection")
        submodule("app/shared/pages/subject-details")
        submodule("app/shared/pages/subject-search")
        submodule("app/shared/pages/subject-cache")
        submodule("app/shared/pages/episode-play")
        submodule("app/shared/pages/home")
        submodule("app/shared/pages/main")
        submodule("app/shared/pages/preferences")
        submodule("app/shared/pages/cache-manage")
    }
}


val generatedResourcesDir = file("build/generated/compose/resourceGenerator/kotlin")

kotlin.sourceSets.commonMain {
    kotlin.srcDirs(generatedResourcesDir)
}
idea {
    module.generatedSourceDirs.add(generatedResourcesDir)
}

kotlin.sourceSets {
    getByName("desktopMain").resources.srcDirs("androidRes/raw")
}

val bangumiClientAndroidAppId = getPropertyOrNull("bangumi.oauth.client.android.appId")
val bangumiClientAndroidSecret = getPropertyOrNull("bangumi.oauth.client.android.secret")

val bangumiClientDesktopAppId = getPropertyOrNull("bangumi.oauth.client.desktop.appId")
val bangumiClientDesktopSecret = getPropertyOrNull("bangumi.oauth.client.desktop.secret")

if (bangumiClientAndroidAppId == null || bangumiClientAndroidSecret == null) {
    logger.warn("bangumi.oauth.client.android.appId or bangumi.oauth.client.android.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
}

if (bangumiClientDesktopAppId == null || bangumiClientDesktopSecret == null) {
    logger.warn("bangumi.oauth.client.desktop.appId or bangumi.oauth.client.desktop.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
}

android {
    namespace = "me.him188.ani"
    compileSdk = getIntProperty("android.compile.sdk")
    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
        buildConfigField("String", "VERSION_NAME", "\"${getProperty("version.name")}\"")
        buildConfigField("String", "BANGUMI_OAUTH_CLIENT_APP_ID", "\"$bangumiClientAndroidAppId\"")
        buildConfigField("String", "BANGUMI_OAUTH_CLIENT_SECRET", "\"$bangumiClientAndroidSecret\"")
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
val generateAniBuildConfigDesktop = tasks.register("generateAniBuildConfigDesktop") {
    val file = buildConfigDesktopDir.get().asFile.resolve("AniBuildConfig.kt").apply {
        parentFile.mkdirs()
        createNewFile()
    }

    inputs.property("project.version", project.version)
    inputs.property("bangumiClientAppIdDesktop", bangumiClientDesktopAppId).optional(true)
    inputs.property("bangumiClientSecret", bangumiClientDesktopSecret).optional(true)

    outputs.file(file)

    val text = """
            package me.him188.ani.app.platform
            object AniBuildConfigDesktop : AniBuildConfig {
                override val versionName = "${project.version}"
                override val bangumiOauthClientAppId = "$bangumiClientDesktopAppId"
                override val bangumiOauthClientSecret = "$bangumiClientDesktopSecret"
                override val isDebug = true
            }
            """.trimIndent()

    outputs.upToDateWhen {
        file.exists() && file.readText().trim() == text.trim()
    }

    doLast {
        file.writeText(text)
    }
}

tasks.named("compileKotlinDesktop") {
    dependsOn(generateAniBuildConfigDesktop)
}
