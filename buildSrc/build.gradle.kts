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
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

sourceSets.main {
    kotlin.srcDir("src")
    resources.srcDir("resources")
}

private val versionsText = project.projectDir.resolve("src/Versions.kt").readText()
fun version(name: String): String {
    return versionsText.lineSequence()
        .map { it.trim() }
        .singleOrNull { it.startsWith("const val $name ") }.let { it ?: error("Cannot find version $name") }
        .substringAfter('"', "")
        .substringBefore('"', "")
        .also {
            check(it.isNotBlank())
            logger.debug("$name=$it")
        }
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.3.6") // Higher versions require too high Kotlin version
    implementation("io.ktor:ktor-client-okhttp:2.3.6") // Higher versions require too high Kotlin version
}

dependencies {
    fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
    fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

    api("org.jetbrains.kotlin", "kotlin-gradle-plugin", version("kotlin")) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    api("com.android.tools.build", "gradle", version("androidGradlePlugin"))
    api("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${version("atomicFU")}")
    // https://mvnrepository.com/artifact/com.android.application/com.android.application.gradle.plugin
    api("com.android.application:com.android.application.gradle.plugin:${version("androidGradlePlugin")}")

    // https://mvnrepository.com/artifact/org.jetbrains.compose/org.jetbrains.compose.gradle.plugin
    api("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:${version("compose")}")

//    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") {
//        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
//        exclude("org.jetbrains.kotlin", "kotlin-reflect")
//        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
//    }

    // https://mvnrepository.com/artifact/com.android.library/com.android.library.gradle.plugin
    api("com.android.library:com.android.library.gradle.plugin:${version("androidGradlePlugin")}")

//    api("gradle.plugin.com.google.gradle:osdetector-gradle-plugin:1.7.0")

    api(gradleApi())
    api(gradleKotlinDsl())
}
