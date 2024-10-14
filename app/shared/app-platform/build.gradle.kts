/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")

    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    idea
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.utils.platform)
        api(libs.kotlinx.coroutines.core)
        api(projects.danmaku.danmakuApi)
        api(libs.kotlinx.collections.immutable)
        api(projects.app.shared.imageViewer)

        api(libs.coil.compose.core)
        api(libs.coil.svg)
        api(libs.coil.network.ktor2)

        api(libs.compose.lifecycle.viewmodel.compose)
        api(libs.compose.lifecycle.runtime.compose)
        api(libs.compose.navigation.compose)
        api(libs.compose.navigation.runtime)
        api(libs.compose.material3.adaptive.core)
        api(libs.compose.material3.adaptive.layout)
        api(libs.compose.material3.adaptive.navigation0)

        api(libs.koin.core)
    }
    sourceSets.commonTest.dependencies {
        api(projects.utils.uiTesting)
    }
    sourceSets.androidMain.dependencies {
        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.compose.ui.tooling)
    }
    sourceSets.desktopMain.dependencies {
        api(libs.jna)
        api(libs.jna.platform)
    }
}

android {
    namespace = "me.him188.ani.app.platform"
}


val aniAuthServerUrlDebug =
    getPropertyOrNull("ani.auth.server.url.debug") ?: "https://auth.myani.org"
val aniAuthServerUrlRelease = getPropertyOrNull("ani.auth.server.url.release") ?: "https://auth.myani.org"

//if (bangumiClientDesktopAppId == null || bangumiClientDesktopSecret == null) {
//    logger.warn("bangumi.oauth.client.desktop.appId or bangumi.oauth.client.desktop.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
//}

android {
    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${getProperty("version.name")}\"")
    }
    buildTypes.getByName("release") {
        isMinifyEnabled = false
        isShrinkResources = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            *sharedAndroidProguardRules(),
        )
        buildConfigField("String", "APP_APPLICATION_ID", "\"me.him188.ani\"")
        buildConfigField("String", "ANI_AUTH_SERVER_URL", "\"$aniAuthServerUrlRelease\"")
    }
    buildTypes.getByName("debug") {
        buildConfigField("String", "APP_APPLICATION_ID", "\"me.him188.ani.debug2\"")
        buildConfigField("String", "ANI_AUTH_SERVER_URL", "\"$aniAuthServerUrlDebug\"")
    }
    buildFeatures {
        buildConfig = true
    }
}

/// BUILD CONFIG

val buildConfigDesktopDir = layout.buildDirectory.file("generated/source/buildConfigDesktop")
val buildConfigIosDir = layout.buildDirectory.file("generated/source/buildConfigIos")

idea {
    module {
        generatedSourceDirs.add(buildConfigDesktopDir.get().asFile)
        generatedSourceDirs.add(buildConfigIosDir.get().asFile)
    }
}

kotlin.sourceSets.getByName("desktopMain") {
    kotlin.srcDirs(buildConfigDesktopDir)
}

val generateAniBuildConfigDesktop = tasks.register("generateAniBuildConfigDesktop") {
    val file = buildConfigDesktopDir.get().asFile.resolve("AniBuildConfig.kt").apply {
        parentFile.mkdirs()
        createNewFile()
    }

    inputs.property("project.version", project.version)

    outputs.file(file)

    val text = """
            package me.him188.ani.app.platform
            object AniBuildConfigDesktop : AniBuildConfig {
                override val versionName = "${project.version}"
                override val isDebug = System.getenv("ANI_DEBUG") == "true" || System.getProperty("ani.debug") == "true"
                override val aniAuthServerUrl = if (isDebug) "$aniAuthServerUrlDebug" else "$aniAuthServerUrlRelease"
            }
            """.trimIndent()

    outputs.upToDateWhen {
        file.exists() && file.readText().trim() == text.trim()
    }

    doLast {
        file.writeText(text)
    }
}

if (enableIos) {
    kotlin.sourceSets.getByName("iosMain") {
        kotlin.srcDirs(buildConfigIosDir)
    }
    val generateAniBuildConfigIos = tasks.register("generateAniBuildConfigIos") {
        val file = buildConfigIosDir.get().asFile.resolve("AniBuildConfig.kt").apply {
            parentFile.mkdirs()
            createNewFile()
        }

        inputs.property("project.version", project.version)

        outputs.file(file)

        val text = """
            package me.him188.ani.app.platform
            object AniBuildConfigIos : AniBuildConfig {
                override val versionName = "${project.version}"
                override val isDebug = false
                override val aniAuthServerUrl = if (isDebug) "$aniAuthServerUrlDebug" else "$aniAuthServerUrlRelease"
            }
            """.trimIndent()

        outputs.upToDateWhen {
            file.exists() && file.readText().trim() == text.trim()
        }

        doLast {
            file.writeText(text)
        }
    }
    tasks.withType(KotlinCompileTool::class) {
        dependsOn(generateAniBuildConfigIos)
    }
} else {
    tasks.register("generateAniBuildConfigIos") {
        inputs.property("project.version", project.version) // 如果没有 input 会不能 cache
    }
}

tasks.named("compileKotlinDesktop") {
    dependsOn(generateAniBuildConfigDesktop)
}

