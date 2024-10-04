/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Compose Multiplatform pre-release versions
}

kotlin {
    jvmToolchain {
        this.vendor.set(findProperty("jvm.toolchain.vendor")?.toString()?.let { JvmVendorSpec.matching(it) })
        this.languageVersion = findProperty("jvm.toolchain.version")?.toString()?.let { JavaLanguageVersion.of(it) }
    }
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi")
    }
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())

    api(libs.kotlin.gradle.plugin) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }

    api(libs.android.gradle.plugin)
    api(libs.atomicfu.gradle.plugin)
    api(libs.android.application.gradle.plugin)
    api(libs.android.library.gradle.plugin)
    api(libs.compose.multiplatfrom.gradle.plugin)
    api(libs.kotlin.compose.compiler.gradle.plugin)
    implementation(kotlin("script-runtime"))
    implementation(libs.snakeyaml)
}
