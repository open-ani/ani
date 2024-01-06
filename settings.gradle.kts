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

rootProject.name = "animation-garden"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Compose Multiplatform pre-release versions
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-atomicfu") { // atomicfu is not on Gradle Plugin Portal
                useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
            }
        }
    }
}

/**
 * 使用的所有依赖的版本. 有关依赖的用途, 查看下面 `versionCatalog` 对这些版本号的使用
 */
@Suppress("ConstPropertyName")
object Versions {
    const val project = "3.0.0-beta1"

    // common
    const val kotlin = "2.0.0-Beta1"
    const val coroutines = "1.6.4"
    const val atomicFU = "0.21.0"
    const val serialization = "1.5.0"
    const val kotlinxDatetime = "0.5.0"
    const val koin = "3.5.3"
    const val slf4j = "2.0.7"
    const val jsoup = "1.15.4"
    const val ktor = "2.3.7" // "3.0.0-beta-1" can't be used since Kamel depends on Ktor 2

    // UI
    const val composeMultiplatform = "1.5.11" // JetBrains Compose Multiplatform
    const val jetpackCompose = "1.5.4" // Google Jetpack Compose

    // Android
    const val androidGradlePlugin = "8.2.0"
    const val android = "4.1.1.4"
    const val androidxAnnotation = "1.6.0"
    const val accompanist: String = "0.33.2-alpha"

    // Server
    const val exposed = "0.45.0"

    object Android {
        const val compileSdk = 34
        const val minSdk = 26
    }
}

dependencyResolutionManagement.versionCatalogs.create("libs") {
    /* Plugins */

    // Don't use Version Catalogs for plugins, since buildSrc has already loaded them into classpath, 
    // and using Version Catalogs will cause resolution ambiguity.
    // For plugin versions, see `build.gradle.kts` from root project
//    plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version(Versions.kotlin)
//    plugin("kotlin-multiplatform", "org.jetbrains.kotlin.multiplatform").version(Versions.kotlin)
//    plugin("kotlin-android", "org.jetbrains.kotlin.android").version(Versions.kotlin)
//    plugin("kotlin.serialization", "org.jetbrains.kotlin.android").version(Versions.kotlin)

    /* Libraries */

    version("kotlin", Versions.kotlin)
    version("compose-compiler", "1.5.6-dev-k2.0.0-Beta1-06a03be2b42")
    version("android-compileSdk", Versions.Android.compileSdk.toString())
    version("android-minSdk", Versions.Android.minSdk.toString())

    // Kotlin
    library("kotlin-test-junit5", "org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlin}")

    // Kotlinx
    library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    library("kotlinx-datetime", "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinxDatetime}")
    library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    library("kotlinx-coroutines-android", "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    library("kotlinx-coroutines-swing", "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.coroutines}")
    library("kotlinx-coroutines-debug", "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.coroutines}")
    library("kotlinx-serialization-core", "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}")
    library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
    library(
        "kotlinx-serialization-protobuf",
        "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.serialization}"
    )
    library("kotlinx-atomicfu-jvm", "org.jetbrains.kotlinx:atomicfu-jvm:${Versions.atomicFU}")

    // Ktor
    library("ktor-client-core", "io.ktor:ktor-client-core:${Versions.ktor}")
    library("ktor-client-cio", "io.ktor:ktor-client-cio:${Versions.ktor}")
    library("ktor-client-logging", "io.ktor:ktor-client-logging:${Versions.ktor}")
    library("ktor-client-websockets", "io.ktor:ktor-client-websockets:${Versions.ktor}")
    library("ktor-client-content-negotiation", "io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
    library("ktor-serialization-kotlinx-json", "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")

    // Koin
    library("koin-core", "io.insert-koin:koin-core:${Versions.koin}")

    // Yamlkt
    library("yamlkt", "net.mamoe.yamlkt:yamlkt:0.12.0")
    library("directories", "dev.dirs:directories:26")

    // Slf4j
    library("slf4j-api", "org.slf4j:slf4j-api:${Versions.slf4j}")
    library("slf4j-simple", "org.slf4j:slf4j-simple:${Versions.slf4j}")

    // Kamel - multiplatform image loading
    library("kamel-image", "media.kamel:kamel-image:0.9.1")

    // Jsoup - HTML parsing (for dmhy)
    library("jsoup", "org.jsoup:jsoup:${Versions.jsoup}")

    // Android Datastore
    library("datastore-preferences", "androidx.datastore:datastore-preferences:1.0.0")
    library("datastore-preferences-core", "androidx.datastore:datastore-preferences-core:1.0.0")

    // Android Accompanist
    library("accompanist-placeholder", "com.google.accompanist:accompanist-placeholder:${Versions.accompanist}")
    library(
        "accompanist-placeholder-material",
        "com.google.accompanist:accompanist-placeholder-material:${Versions.accompanist}"
    )

    // Android-only UI libraries
    // Each library has its own version, so we don't use `Versions` here.
    library("androidx-core-ktx", "androidx.core:core-ktx:1.12.0")
    library("androidx-activity-compose", "androidx.activity:activity-compose:1.8.2")
    library("androidx-appcompat", "androidx.appcompat:appcompat:1.6.1")
    library("androidx-swiperefreshlayout", "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    library("androidx-material", "com.google.android.material:material:1.11.0")
    library("androidx-material3-window-size-class0", "androidx.compose.material3:material3-window-size-class:1.1.2")
    library("androidx-activity-compose", "androidx.activity:activity-compose:1.8.2")
    library("androidx-navigation-compose", "androidx.navigation:navigation-compose:2.7.6")

    val composeVersion = Versions.jetpackCompose
    library("androidx-compose-ui", "androidx.compose.ui:ui:$composeVersion")
    library("androidx-compose-ui-tooling", "androidx.compose.ui:ui-tooling:$composeVersion")
    library("androidx-compose-ui-viewbinding", "androidx.compose.ui:ui-viewbinding:$composeVersion")
    library("androidx-compose-foundation", "androidx.compose.foundation:foundation:$composeVersion")
    library("androidx-compose-material", "androidx.compose.material:material:$composeVersion")
    library("androidx-compose-material3", "androidx.compose.material3:material3:1.2.0-beta01")
    library("androidx-compose-ui-tooling-preview", "androidx.compose.ui:ui-tooling-preview:${composeVersion}")
    // library("compose-runtime-livedata", "androidx.compose.runtime:runtime-livedata:$composeVersion")
}

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

// Protocol shared by client and server (targets JVM)
includeProject(":protocol", "protocol")

// Utilities shared by client and server (targeting JVM)
includeProject(":utils:slf4j-kt", "utils/slf4j-kt") // shared by client and server (targets JVM)
includeProject(
    ":utils:serialization",
    "utils/serialization"
)

// client
includeProject(":app:shared", "app/shared") // shared by clients (targets JVM)
includeProject(":app:desktop", "app/desktop") // desktop JVM client for macOS, Windows, and Linux
includeProject(":app:android", "app/android") // Android client

// server
//includeProject(":server:core", "server/core") // server core
//includeProject(":server:database", "server/database") // server database interfaces
//includeProject(":server:database-xodus", "server/database-xodus") // database implementation with Xodus

// data sources
includeProject(":data-sources:api", "data-sources/api") // data source interfaces
includeProject(":data-sources:dmhy", "data-sources/dmhy") // data source from https://dmhy.org
includeProject(
    ":data-sources:bangumi",
    "data-sources/bangumi"
) // data source from https://bangumi.tv

includeProject(
    ":data-sources:dmhy:dataset-tools",
    "data-sources/dmhy/dataset-tools"
) // tools for generating dataset for ML title parsing

// ci
includeProject(":ci-helper", "ci-helper") // 

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
