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

import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
    application
}

dependencies {
    // ktor
    val ktorVersion = Versions.ktor
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")

    implementation(projects.server.databaseXodus)
}

application {
    mainClass.set("me.him188.animationgarden.server.ServerMain")
}

tasks.withType(KotlinJvmCompile::class) {
    kotlinOptions.jvmTarget = "11"
}

kotlin.sourceSets.all {
    languageSettings.enableLanguageFeature("ContextReceivers")
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
}