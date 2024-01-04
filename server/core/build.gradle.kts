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
    application
}

dependencies {
    api(`kotlinx-coroutines-core`)
    api(`kotlinx-serialization-json`)
    api(`kotlinx-datetime`)

    // submodules
    api(projects.server.databaseXodus)
    api(projects.server.database)
    api(projects.protocol)

    // dependency injection
    api(`koin-core`)

    // logging
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation(projects.utils.slf4jKt)

    // ktor
    @Suppress("LocalVariableName") val ktor_version = Versions.ktor
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")

    // cli
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    // data sources
    implementation(projects.dataSources.dmhy)
    implementation(projects.dataSources.bangumi)

    // utils
    implementation("de.mkammerer:argon2-jvm:2.5")
}

application {
    mainClass.set("me.him188.animationgarden.server.ServerMain")
}
