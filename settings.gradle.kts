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

rootProject.name = "ani"

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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

// Utilities shared by client and server (targeting JVM)
includeProject(":utils:slf4j-kt", "utils/slf4j-kt") // shared by client and server (targets JVM)
includeProject(":utils:serialization", "utils/serialization")
includeProject(":utils:coroutines", "utils/coroutines")
includeProject(":utils:ktor-client", "utils/ktor-client")
includeProject(":utils:io", "utils/io")
includeProject(":utils:testing", "utils/testing")


// TODO: add torrent modules back when kotlin has fixed
//includeProject(":torrent:api") // Torrent 系统 API
//includeProject(":torrent:impl:libtorrent4j") // libtorrent4j 实现
//includeProject(":torrent:impl:qbittorrent") // qBittorrent 实现

// client
includeProject(":app:shared", "app/shared") // shared by clients (targets JVM)
includeProject(":app:desktop", "app/desktop") // desktop JVM client for macOS, Windows, and Linux
includeProject(":app:android", "app/android") // Android client

// server
//includeProject(":server:core", "server/core") // server core
//includeProject(":server:database", "server/database") // server database interfaces
//includeProject(":server:database-xodus", "server/database-xodus") // database implementation with Xodus

// data sources
includeProject(":data-sources:api", "data-sources/api") // data source interfaces: Media, MediaSource 
includeProject(":data-sources:api:test-codegen") // 生成单元测试
includeProject(":data-sources:core", "data-sources/core") // data source managers: MediaFetcher, MediaCacheStorage
includeProject(":data-sources:dmhy", "data-sources/dmhy") // data source from https://dmhy.org
includeProject(":data-sources:acg-rip", "data-sources/acg.rip") // data source from https://acg.rip
includeProject(":data-sources:mikan", "data-sources/mikan") // data source from https://mikanani.me/
includeProject(
    ":data-sources:bangumi",
    "data-sources/bangumi"
) // data source from https://bangumi.tv

// danmaku
//includeProject(":danmaku:api", "danmaku/api") // danmaku source interfaces
//includeProject(":danmaku:ui", "danmaku/ui") // danmaku UI composable
//includeProject(":danmaku:dandanplay", "danmaku/dandanplay")
//includeProject(":danmaku:ani:client", "danmaku/ani/client") // danmaku server
includeProject(":danmaku:ani:server", "danmaku/ani/server") // danmaku server
includeProject(":danmaku:ani:protocol", "danmaku/ani/protocol") // danmaku server-client protocol

includeProject(
    ":data-sources:dmhy:dataset-tools",
    "data-sources/dmhy/dataset-tools"
) // tools for generating dataset for ML title parsing

// ci
includeProject(":ci-helper", "ci-helper") // 

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
