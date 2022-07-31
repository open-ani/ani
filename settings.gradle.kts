// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

rootProject.name = "animation-garden-desktop"

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
}

includeProject(":animation-garden-api", "api")
includeProject(":animation-garden-app", "app")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
