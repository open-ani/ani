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
    kotlin("plugin.serialization")
    `ani-mpp-lib-targets`
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(libs.kotlinx.serialization.core)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
            api(projects.utils.logging)
            implementation(projects.utils.platform)
        }
    }

    sourceSets.jvmMain {
        dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }

    sourceSets.appleMain {
        dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}