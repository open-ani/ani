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
    `android-library`
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    // 注意! 前几个插件顺序非常重要, 调整后可能导致 compose multiplatform resources 生成错误
    kotlin("plugin.serialization")

    `ani-mpp-lib-targets`

    // for @Stable and @Immutable
    // Note: we actually can avoid this, by using a `compose_compiler_config.conf`
    // See https://developer.android.com/develop/ui/compose/performance/stability/fix#configuration-file
    // But for simplicity, we just include compose here.
    `ani-compose-hmpp`

    idea
}

android {
    namespace = "me.him188.ani.data.sources.api"
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            implementation(libs.kotlinx.serialization.core)
            api(libs.kotlinx.coroutines.core)
            api(projects.utils.ktorClient)
            api(projects.utils.serialization)
            implementation(projects.utils.platform)
            api(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(projects.utils.logging)

            implementation(compose.runtime) // required by the compose compiler
        }
    }

    sourceSets.commonTest {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.utils.testing)
        }
    }

    sourceSets.jvmMain {
        dependencies {
            api(libs.jsoup)
        }
    }
}
idea {
    module.generatedSourceDirs.add(file("test/title/generated"))
}
