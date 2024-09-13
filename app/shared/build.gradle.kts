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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool


plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    // 注意! 前几个插件顺序非常重要, 调整后可能导致 compose multiplatform resources 生成错误

    `ani-mpp-lib-targets`

    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    idea
}

compose.resources {
    packageOfResClass = "me.him188.ani.app"
    generateResClass = always
}

atomicfu {
    transformJvm = false // 这东西很不靠谱, 等 atomicfu 正式版了可能可以考虑下
}

val enableIosFramework = getPropertyOrNull("ani.build.framework") != "false"

kotlin {
    if (enableIosFramework) {
        listOf(
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = false
            }
        }
    }

    sourceSets.commonMain.dependencies {
        api(projects.utils.platform)
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.serialization.json.io)
        api(libs.kotlinx.serialization.protobuf)
        implementation(libs.atomicfu) // room runtime
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.io.core)
        api(libs.kotlinx.collections.immutable)
        api(projects.utils.ktorClient)

        api(projects.app.shared.appBase)
        api(projects.app.shared.appData)
        api(projects.app.shared.placeholder)
        api(projects.app.shared.uiFoundation)
        api(projects.app.shared.videoPlayer)
        api(projects.app.shared.videoPlayer.torrentSource)

        // Compose
        api(compose.foundation)
        api(compose.animation)
        api(compose.ui)
        api(compose.material3)
        api(compose.materialIconsExtended)
        api(compose.runtime)
        api(libs.compose.lifecycle.viewmodel.compose)
        api(libs.compose.lifecycle.runtime.compose)
        api(libs.compose.navigation.compose)
        api(libs.compose.navigation.runtime)
        api(libs.compose.material3.adaptive.core.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        api(libs.compose.material3.adaptive.layout.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        api(libs.compose.material3.adaptive.navigation0.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        implementation(compose.components.resources)
        implementation(compose.material3AdaptiveNavigationSuite)
        implementation(libs.reorderable)

        // Data sources
        api(projects.datasource.datasourceApi)
        api(projects.datasource.datasourceCore)
        api(projects.datasource.bangumi)
        api(projects.datasource.mikan)

        api(projects.client)
        api(projects.utils.logging)
        api(projects.utils.coroutines)
        api(projects.utils.io)
        api(projects.app.shared.imageViewer)
        api(projects.utils.xml)
        api(projects.utils.bbcode)
        api(projects.danmaku.danmakuApi)
        api(projects.danmaku.dandanplay)
        api(projects.danmaku.danmakuUi)
        api(projects.utils.ipParser)
        api(projects.torrent.torrentApi)
        api(projects.torrent.anitorrent)

        // Ktor
        api(libs.ktor.client.websockets)
        api(libs.ktor.client.logging)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.serialization.kotlinx.json)

        // Others
        api(libs.koin.core) 
        api(libs.directories) // Data directories on all OSes
        api(libs.coil.core)
        api(libs.coil.svg)
        api(libs.coil.compose.core)
        api(libs.coil.network.ktor2)
        implementation(libs.constraintlayout.compose)
    }

    // shared by android and desktop
    sourceSets.getByName("jvmMain").dependencies {
        // TODO: to be commonized
        api(projects.datasource.dmhy)
        api(projects.datasource.acgRip)
        api(projects.datasource.nyafun)
        api(projects.datasource.mxdongman)
        api(projects.datasource.ntdm)
        api(projects.datasource.gugufan)
        api(projects.datasource.xfdm)
        api(projects.datasource.jellyfin)
        api(projects.datasource.ikaros)

        implementation(libs.jna)
        implementation(libs.slf4j.api)
        api(libs.ktor.client.okhttp)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.utils.testing)
        implementation(projects.utils.uiTesting)
    }

    sourceSets.androidMain.dependencies {
        api(libs.kotlinx.coroutines.android)
        api(libs.datastore)
        api(libs.datastore.preferences)
        api(libs.androidx.appcompat)
        api(libs.androidx.media)
        api(libs.androidx.core.ktx)
        api(libs.androidx.activity.compose)
        api(libs.androidx.activity.ktx)
        api(libs.koin.android)
        implementation(libs.androidx.browser)

        // Compose
        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.compose.material3)

        api(libs.coil)

        implementation(libs.androidx.media3.ui)
        implementation(libs.androidx.media3.exoplayer)

        api(libs.logback.android)
    }

    sourceSets.androidUnitTest.dependencies {
        implementation(libs.mockito)
        implementation(libs.mockito.kotlin)
        implementation(libs.koin.test)
    }

    sourceSets.nativeMain.dependencies {
        implementation(libs.stately.common) // fixes koin bug
    }

    sourceSets.named("desktopMain").dependencies {
        api(compose.desktop.currentOs) {
            exclude(compose.material) // We use material3
        }
        api(compose.material3)
        api("org.jetbrains.compose.ui:ui-graphics-desktop:${libs.versions.compose.multiplatform.get()}")
        api(projects.utils.logging)
        api(libs.kotlinx.coroutines.swing)
        implementation(libs.vlcj)
        implementation(libs.jna) // required and don't change version, otherwise vlcj might crash the VM 

        runtimeOnly(libs.kotlinx.coroutines.debug)

        implementation(libs.log4j.core)
        implementation(libs.log4j.slf4j.impl)

        implementation(libs.ktor.serialization.kotlinx.json)

        implementation(libs.selenium.java)
        implementation(libs.webdrivermanager)

        implementation(libs.filekit.core)
        implementation(libs.filekit.compose)
    }
}

// RESOURCES

idea {
    val generatedResourcesDir = file("build/generated/compose/resourceGenerator/kotlin")
    module {
        generatedSourceDirs.add(generatedResourcesDir.resolve("commonMainResourceAccessors"))
        generatedSourceDirs.add(generatedResourcesDir.resolve("commonResClass"))
    }
}

// AS 问题 since Compose 1.7.0-beta03
afterEvaluate {
    tasks.matching { it.name.contains("generateReleaseLintVitalModel") }.all {
        dependsOn("releaseAssetsCopyForAGP")
    }
    tasks.matching { it.name.contains("lintVitalAnalyzeRelease") }.all {
        dependsOn("releaseAssetsCopyForAGP")
    }
}


// BUILD CONFIG

//if (bangumiClientDesktopAppId == null || bangumiClientDesktopSecret == null) {
//    logger.warn("bangumi.oauth.client.desktop.appId or bangumi.oauth.client.desktop.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
//}

android {
    namespace = "me.him188.ani"
    compileSdk = getIntProperty("android.compile.sdk")
    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
    }
    buildTypes.getByName("release") {
        isMinifyEnabled = false // shared 不能 minify, 否则构建 app 会失败
        isShrinkResources = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            *sharedAndroidProguardRules(),
        )
    }
    buildTypes.getByName("debug") {
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
}


// 太耗内存了, 只能一次跑一个
// compose bug, 不能用这个 https://youtrack.jetbrains.com/issue/CMP-5835
//tasks.filter { it.name.contains("link") && it.name.contains("Framework") && it.name.contains("Ios") }
//    .sorted()
//    .let { links ->
//        links.forEachIndexed { index, task ->
//            for (index1 in (index + 1)..links.lastIndex) {
//                task.mustRunAfter(links[index1])
//            }
//        }
//    }

if (enableIosFramework) {
// 太耗内存了, 只能一次跑一个
    tasks.named("linkDebugFrameworkIosArm64") {
        mustRunAfter("linkReleaseFrameworkIosArm64")
        mustRunAfter("linkDebugFrameworkIosSimulatorArm64")
        mustRunAfter("linkReleaseFrameworkIosSimulatorArm64")
    }
    tasks.named("linkReleaseFrameworkIosArm64") {
        mustRunAfter("linkDebugFrameworkIosSimulatorArm64")
        mustRunAfter("linkReleaseFrameworkIosSimulatorArm64")
    }
    tasks.named("linkDebugFrameworkIosSimulatorArm64") {
        mustRunAfter("linkReleaseFrameworkIosSimulatorArm64")
    }
}