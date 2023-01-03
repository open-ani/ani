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

@file:Suppress("UnstableApiUsage")

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

fun HttpRequestBuilder.contentType(type: String) {
    return contentType(ContentType.parse(type))
}



plugins {
    kotlin("jvm")
    id("kotlinx-atomicfu")
}

kotlin.sourceSets.all {
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
    languageSettings.optIn("androidx.compose.animation.ExperimentalAnimationApi")
    languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
    languageSettings.enableLanguageFeature("ContextReceivers")
}

object ArtifactNamer {
    fun getFullVersionFromTag(tag: String): String {
        return tag.substringAfter("v")
    }

    // fullVersion example: 2.0.0-beta03
    fun androidApp(fullVersion: String): String {
        return "AnimationGarden-$fullVersion.apk"
    }
}

tasks.register("uploadAndroidApk") {
    doLast {
        val tag = getProperty("ci-helper.tag")
        val fullVersion = ArtifactNamer.getFullVersionFromTag(tag)
        val uploadUrl = getProperty("ci-helper.upload-url")
//        val repository = getProperty("GITHUB_REPOSITORY")
        val token = getProperty("GITHUB_TOKEN")

        println("tag = $tag")
        println("fullVersion = $fullVersion")
        println("uploadUrl = $uploadUrl")
        println("token = ${token.isNotEmpty()}")

        runBlocking {
            val resp = HttpClient().post(uploadUrl) {
                header("Authorization", "Bearer $token")
                header("Accept", "application/vnd.github+json")
                parameter("name", ArtifactNamer.androidApp(fullVersion))
                contentType("application/vnd.android.package-archive")
                setBody(
                    project(":android").buildDir.resolve("outputs/apk/release").walk()
                        .single { it.extension == "apk" && it.name.contains("release") }
                )
            }
            check(resp.status.isSuccess()) {
                "Request $uploadUrl failed with ${resp.status}. Response: ${
                    resp.runCatching { bodyAsText() }.getOrNull()
                }"
            }
        }
    }
}

fun getProperty(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: properties[name]?.toString()
//        ?: getLocalProperty(name)
        ?: ext.get(name).toString()
