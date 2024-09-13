import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

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

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `ani-mpp-lib-targets`
    id("org.jetbrains.kotlinx.atomicfu")
    kotlin("plugin.serialization")
}

android {
    namespace = "me.him188.ani.utils.io"
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions.freeCompilerArgs.add("-Xdont-warn-on-error-suppression")

    sourceSets.commonMain.dependencies {
        api(projects.utils.platform)
        api(libs.kotlinx.io.core)
        implementation(libs.atomicfu)
//        implementation(libs.okio) // 仅用于读文件
    }

    sourceSets.nativeMain.dependencies {
        api(libs.korlibs.crypto) // JVM 用 JDK 就够了
    }
}
