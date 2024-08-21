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

import com.google.devtools.ksp.gradle.KspTaskJvm
import com.google.devtools.ksp.gradle.KspTaskNative
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool


plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    // 注意! 前几个插件顺序非常重要, 调整后可能导致 compose multiplatform resources 生成错误
    `flatten-source-sets`

    `ani-mpp-lib-targets`

    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    id("com.google.devtools.ksp")
    id("androidx.room")
    idea
}

extra.set("ani.jvm.target", 17)

compose.resources {
    packageOfResClass = "me.him188.ani.app"
    generateResClass = always
}

atomicfu {
    transformJvm = false // 这东西很不靠谱, 等 atomicfu 正式版了可能可以考虑下
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.serialization.json.io)
        implementation(libs.atomicfu) // room runtime
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.io.core)
        api(libs.kotlinx.collections.immutable)
        api(projects.utils.ktorClient)

        // Compose
        api(compose.foundation)
        api(compose.animation)
        api(compose.ui)
        api(compose.material3)
        api(compose.materialIconsExtended)
        api(compose.runtime)
        implementation(compose.components.resources)
        implementation(libs.reorderable)

        // Data sources
        api(projects.dataSources.api)
        api(projects.dataSources.core)
        api(projects.dataSources.bangumi)
        api(projects.dataSources.mikan)

        api(projects.client)
        api(projects.utils.logging)
        api(projects.utils.coroutines)
        api(projects.utils.io)
        api(projects.app.shared.imageViewer)

        // Ktor
        api(libs.ktor.client.websockets)
        api(libs.ktor.client.logging)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.serialization.kotlinx.json)

        // Others
        api(libs.koin.core) // dependency injection
        api(libs.directories) // Data directories on all OSes
        api(libs.coil.core)
        api(libs.coil.svg)
        api(libs.coil.compose.core)
        api(libs.coil.network.ktor2)
        api(libs.datastore.core) // Data Persistence
        api(libs.datastore.preferences.core) // Preferences
        api(libs.precompose) // Navigator
        api(libs.precompose.koin) // Navigator
        api(libs.precompose.viewmodel) // Navigator
        implementation(libs.androidx.room.runtime.get().toString()) {
            exclude("org.jetbrains.kotlinx", "atomicfu")
        } // multi-platform database
        api(libs.sqlite.bundled) // database driver implementation

        // Torrent
        implementation(libs.bencode)

        implementation(libs.constraintlayout.compose)

        implementation(libs.jna)

        implementation(libs.slf4j.api)

        implementation(projects.utils.bbcode)
    }

    // shared by android and desktop
    sourceSets.getByName("jvmMain").dependencies {
        // TODO: to be commonized
        api(projects.torrent.anitorrent)
        api(projects.dataSources.dmhy)
        api(projects.dataSources.acgRip)
        api(projects.dataSources.nyafun)
        api(projects.dataSources.mxdongman)
        api(projects.dataSources.ntdm)
        api(projects.dataSources.gugufan)
        api(projects.dataSources.xfdm)
        api(projects.dataSources.jellyfin)
        api(projects.dataSources.ikaros)

        api(libs.ktor.client.okhttp)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.utils.testing)
    }

    sourceSets.androidMain.dependencies {
        api(libs.kotlinx.coroutines.android)
        api(libs.datastore)
        api(libs.datastore.preferences)
        api(libs.androidx.appcompat)
        api(libs.androidx.media)
        api(libs.androidx.core.ktx)
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
//        implementation(libs.vlcj.javafx)
//        implementation(libs.javafx.controls)
//        implementation(libs.javafx.graphics)

        // https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/17.0.11/
        val os = getOs()

        runtimeOnly(libs.kotlinx.coroutines.debug)

        implementation(libs.log4j.core)
        implementation(libs.log4j.slf4j.impl)

        implementation(libs.ktor.serialization.kotlinx.json)

        implementation(libs.selenium.java)
        implementation(libs.webdrivermanager)

        implementation(libs.filekit.core)
        implementation(libs.filekit.compose)
//        implementation(libs.htmlunit)
//        implementation("org.openjfx:javafx-base:17.0.11:$classifier") {
//            exclude("org.openjfx")
//        }
//        implementation("org.openjfx:javafx-controls:17.0.11:$classifier") {
//            exclude("org.openjfx")
//        }
//        implementation("org.openjfx:javafx-graphics:17.0.11:$classifier") {
//            exclude("org.openjfx")
//        }
//        implementation("org.openjfx:javafx-media:17.0.11:$classifier") {
//            exclude("org.openjfx")
//        }
//        implementation("org.openjfx:javafx-web:17.0.11:$classifier") {
//            exclude("org.openjfx")
//        }
    }

    sourceSets {
        // TODO: 临时解决方案, KT-65362 Cannot resolve declarations from a dependency when there are multiple JVM-only project dependencies in a JVM-Android MPP
        //  https://youtrack.jetbrains.com/issue/KT-65362
        // Danmaku

        fun submodule(dir: String, flatten: Boolean = !rootProject.projectDir.resolve("$dir/src/commonMain").exists()) {
            if (flatten) {
                // flatten
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
                getByName("iosMain") {
                    kotlin.srcDirs(rootProject.projectDir.resolve("$dir/ios/"))
                }
            } else {
                sourceSets.all {
                    kotlin.srcDirs(rootProject.projectDir.resolve("$dir/src/${this.name}/kotlin"))
                    resources.srcDirs(rootProject.projectDir.resolve("$dir/src/${this.name}/resources"))
                }
            }
        }

        submodule("danmaku/api", flatten = false)
        submodule("danmaku/dandanplay")
        submodule("danmaku/ui")

        submodule("torrent/api")
        submodule("torrent/impl/anitorrent")

        submodule("app/shared/placeholder")
        submodule("app/shared/video-player")
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

room {
    schemaDirectory("$projectDir/schemas")
}


// BUILD CONFIG

val bangumiClientAndroidAppId = getPropertyOrNull("bangumi.oauth.client.android.appId")
val bangumiClientAndroidSecret = getPropertyOrNull("bangumi.oauth.client.android.secret")

val bangumiClientDesktopAppId = getPropertyOrNull("bangumi.oauth.client.desktop.appId")
val bangumiClientDesktopSecret = getPropertyOrNull("bangumi.oauth.client.desktop.secret")

val aniAuthServerUrlDebug =
    getPropertyOrNull("ani.auth.server.url.debug") ?: "https://auth.myani.org"
val aniAuthServerUrlRelease = getPropertyOrNull("ani.auth.server.url.release") ?: "https://auth.myani.org"

if (bangumiClientAndroidAppId == null || bangumiClientAndroidSecret == null) {
    logger.warn("i:: bangumi.oauth.client.android.appId or bangumi.oauth.client.android.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
}

//if (bangumiClientDesktopAppId == null || bangumiClientDesktopSecret == null) {
//    logger.warn("bangumi.oauth.client.desktop.appId or bangumi.oauth.client.desktop.secret is not set. Bangumi authorization will not work. Get a token from https://bgm.tv/dev/app and set them in local.properties.")
//}

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
        isMinifyEnabled = false // shared 不能 minify, 否则构建 app 会失败
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    add("kspDesktop", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
//    add("kspIosArm64", libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}


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

kotlin.sourceSets.iosMain {
    kotlin.srcDirs(buildConfigIosDir)
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

val generateAniBuildConfigIos = tasks.register("generateAniBuildConfigIos") {
    val file = buildConfigIosDir.get().asFile.resolve("AniBuildConfig.kt").apply {
        parentFile.mkdirs()
        createNewFile()
    }

    inputs.property("project.version", project.version)
    inputs.property("bangumiClientAppIdDesktop", bangumiClientDesktopAppId).optional(true)
    inputs.property("bangumiClientSecret", bangumiClientDesktopSecret).optional(true)

    outputs.file(file)

    val text = """
            package me.him188.ani.app.platform
            object AniBuildConfigIos : AniBuildConfig {
                override val versionName = "${project.version}"
                override val bangumiOauthClientAppId = "$bangumiClientDesktopAppId"
                override val bangumiOauthClientSecret = "$bangumiClientDesktopSecret"
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

tasks.named("compileKotlinDesktop") {
    dependsOn(generateAniBuildConfigDesktop)
    if (enableAnitorrent) {
        mustRunAfter(":torrent:anitorrent:generateSwigImpl") // TODO: move this to impl:anitorrent module when we have separate modules
    }
}

tasks.withType(KotlinCompileTool::class) {
    dependsOn(generateAniBuildConfigIos)
}

// :app:shared:kspKotlinDesktop
tasks.withType(KspTaskJvm::class.java) {
    dependsOn(generateAniBuildConfigDesktop)
}

tasks.withType(KspTaskNative::class.java) {
    dependsOn(generateAniBuildConfigIos)
}

