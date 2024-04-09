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
    kotlin("jvm")
    kotlin("plugin.serialization")

    // for @Stable and @Immutable
    // Note: we actually can avoid this, by using a `compose_compiler_config.conf`
    // See https://developer.android.com/develop/ui/compose/performance/stability/fix#configuration-file
    // But for simplicity, we just include compose here.
    id("org.jetbrains.compose")
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    api(libs.kotlinx.coroutines.core)
    api(projects.utils.ktorClient)
    implementation(projects.utils.slf4jKt)

    implementation(compose.runtime) // required by the compose compiler
}
