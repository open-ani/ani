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

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
}

dependencies {
    api(`kotlinx-coroutines`)
    api(`kotlinx-serialization-json`)
    api(`kotlinx-datetime`)
    api(`koin-core`)

    api(projects.server.database)

    // xodus

    api("org.jetbrains.xodus:xodus-openAPI:2.0.1")
    api("org.jetbrains.xodus:xodus-entity-store:2.0.1")
    api("org.jetbrains.xodus:xodus-environment:2.0.1")
    api("org.jetbrains.xodus:dnq:2.0.0")

    // exposed
//    val exposedVersion = Versions.exposed
//    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
}
