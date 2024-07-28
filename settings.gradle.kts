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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

// Utilities shared by client and server (targeting JVM)
includeProject(":utils:platform") // 适配各个平台的基础 API
includeProject(":utils:logging") // shared by client and server (targets JVM)
includeProject(":utils:serialization", "utils/serialization")
includeProject(":utils:coroutines", "utils/coroutines")
includeProject(":utils:ktor-client", "utils/ktor-client")
includeProject(":utils:io", "utils/io")
includeProject(":utils:testing", "utils/testing")
includeProject(":utils:bbcode", "utils/bbcode")
includeProject(":utils:bbcode:test-codegen")


// TODO: add torrent modules back when kotlin has fixed
//includeProject(":torrent:api") // Torrent 系统 API
//includeProject(":torrent:impl:libtorrent4j") // libtorrent4j 实现
//includeProject(":torrent:impl:qbittorrent") // qBittorrent 实现
includeProject(":torrent:anitorrent")

// client
includeProject(":app:shared", "app/shared") // shared by clients (targets JVM)
includeProject(":app:desktop", "app/desktop") // desktop JVM client for macOS, Windows, and Linux
includeProject(":app:android", "app/android") // Android client
includeProject(":app:shared:image-viewer")
includeProject(":client")

// server
//includeProject(":server:core", "server/core") // server core
//includeProject(":server:database", "server/database") // server database interfaces
//includeProject(":server:database-xodus", "server/database-xodus") // database implementation with Xodus

// data sources
includeProject(":data-sources:api") // data source interfaces: Media, MediaSource 
includeProject(":data-sources:api:test-codegen") // 生成单元测试
includeProject(":data-sources:core", "data-sources/core") // data source managers: MediaFetcher, MediaCacheStorage
includeProject(":data-sources:bangumi", "data-sources/bangumi") // https://bangumi.tv
//   BT 数据源
includeProject(":data-sources:dmhy", "data-sources/bt/dmhy") // https://dmhy.org
includeProject(":data-sources:acg-rip", "data-sources/bt/acg.rip") // https://acg.rip
includeProject(":data-sources:mikan", "data-sources/bt/mikan") // https://mikanani.me/
//   Web 数据源
includeProject(":data-sources:web-base", "data-sources/web/web-base") // web 基础
includeProject(":data-sources:nyafun", "data-sources/web/nyafun") // https://nyafun.net/
includeProject(":data-sources:mxdongman", "data-sources/web/mxdongman") // https://mxdm4.com/
includeProject(":data-sources:ntdm", "data-sources/web/ntdm") // https://ntdm.tv/
includeProject(":data-sources:gugufan", "data-sources/web/gugufan")
includeProject(":data-sources:jellyfin", "data-sources/jellyfin")
includeProject(":data-sources:ikaros", "data-sources/ikaros") // https://ikaros.run/

// danmaku
//includeProject(":danmaku:api", "danmaku/api") // danmaku source interfaces
//includeProject(":danmaku:ui", "danmaku/ui") // danmaku UI composable
//includeProject(":danmaku:dandanplay", "danmaku/dandanplay")
//includeProject(":danmaku:ani:client", "danmaku/ani/client") // danmaku server

includeProject(
    ":data-sources:dmhy:dataset-tools",
    "data-sources/bt/dmhy/dataset-tools",
) // tools for generating dataset for ML title parsing

// ci
includeProject(":ci-helper", "ci-helper") // 

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
