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

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "animation-garden"

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

includeProject(":protocol", "protocol") // shared by client and server (targets JVM)
includeProject(":utils:slf4j-kt", "utils/slf4j-kt") // shared by client and server (targets JVM)

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
includeProject(":data-sources:bangumi", "data-sources/bangumi") // data source from https://bangumi.tv

includeProject(
    ":data-sources:dmhy:dataset-tools",
    "data-sources/dmhy/dataset-tools"
) // tools for generating dataset for ML title parsing

// ci
includeProject(":ci-helper", "ci-helper") // 

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
