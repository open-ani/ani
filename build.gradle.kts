import org.jetbrains.compose.ComposeExtension

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

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    kotlin("jvm") apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    id("org.jetbrains.compose") apply false
    id("com.android.library") apply false
    id("com.android.application") apply false
}

allprojects {
    group = "me.him188.ani"
    version = properties["version.name"].toString()

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
        google()
    }
}


extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

subprojects {
    afterEvaluate {
        kotlin.runCatching { configureFlattenSourceSets() }
        kotlin.runCatching { configureKotlinOptIns() }
        configureKotlinTestSettings()
        configureEncoding()
        configureJvmTarget()
        configureCompose()
        kotlin.runCatching {
            extensions.findByType(ComposeExtension::class)?.apply {
                this.kotlinCompilerPlugin.set(libs.versions.compose.multiplatform.compiler.get())
            }
        }
    }
}