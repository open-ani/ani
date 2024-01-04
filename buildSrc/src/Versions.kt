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

@file:Suppress("ObjectPropertyName", "MemberVisibilityCanBePrivate", "ConstPropertyName")

object Versions {
    val project = "3.0.0-beta1"
//        System.getenv("ag.build.project.version")?.takeIf { it.isNotBlank() }
//        ?: System.getProperty("mirai.build.project.version")?.takeIf { it.isNotBlank() }
//        ?: /*PROJECT_VERSION_START*/"2.15.0"/*PROJECT_VERSION_END*/

    // common
    const val kotlin = "2.0.0-Beta1"
    const val coroutines = "1.6.4"
    const val atomicFU = "0.23.1"
    const val serialization = "1.5.0"
    const val kotlinxDatetime = "0.5.0"
    const val koin = "3.5.3"

    const val slf4j = "2.0.7"

    // UI
    const val compose = "1.5.11" // Gradle plugin version

    // Android
    const val androidGradlePlugin = "8.2.0"
    const val android = "4.1.1.4"
    const val androidxAnnotation = "1.6.0"

    // Server
    const val exposed = "0.45.0"
    const val ktor = "3.0.0-beta-1"

    const val junit = "5.7.2"
}

const val `kotlinx-datetime` = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinxDatetime}"
const val `kotlinx-coroutines-core` =
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
const val `kotlinx-coroutines-android` =
    "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
const val `kotlinx-coroutines-swing` =
    "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.coroutines}"
const val `kotlinx-coroutines-debug` =
    "org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.coroutines}"
const val `kotlinx-serialization-core` =
    "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}"
const val `kotlinx-serialization-json` =
    "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}"
const val `kotlinx-serialization-protobuf` =
    "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.serialization}"

const val `koin-core` = "io.insert-koin:koin-core:${Versions.koin}"
const val `slf4j-api` = "org.slf4j:slf4j-api:${Versions.slf4j}"
const val `slf4j-simple` = "org.slf4j:slf4j-simple:${Versions.slf4j}"

const val `ktor-client-core` = "io.ktor:ktor-client-core:${Versions.ktor}"
const val `ktor-client-cio` = "io.ktor:ktor-client-cio:${Versions.ktor}"
const val `ktor-client-logging` = "io.ktor:ktor-client-logging:${Versions.ktor}"
const val `ktor-client-websockets` = "io.ktor:ktor-client-websockets:${Versions.ktor}"
const val `ktor-client-content-negotiation` =
    "io.ktor:ktor-client-content-negotiation:${Versions.ktor}"
const val `ktor-serialization-kotlinx-json` =
    "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}"

const val `kotlin-test-junit5` = "org.jetbrains.kotlin:kotlin-test-junit5:${Versions.kotlin}"
const val `junit-jupiter-api` = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
const val `junit-jupiter-params` = "org.junit.jupiter:junit-jupiter-params:${Versions.junit}"
const val `junit-jupiter-engine` = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"
