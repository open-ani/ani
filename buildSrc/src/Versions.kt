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


object Versions {
    val project = System.getenv("mirai.build.project.version")?.takeIf { it.isNotBlank() }
        ?: System.getProperty("mirai.build.project.version")?.takeIf { it.isNotBlank() }
        ?: /*PROJECT_VERSION_START*/"2.15.0"/*PROJECT_VERSION_END*/
    // DO NOT ADD SPACE BETWEEN!

    const val kotlinCompiler = "2.0.0-Beta1"
    const val kotlinStdlib = kotlinCompiler
    const val kotlinLanguageVersionForTests = "2.0"

    const val coroutines = "1.6.4"
    const val atomicFU = "0.20.0"
    const val serialization = "1.5.0"

    const val androidGradlePlugin = "8.2.0"
    const val android = "4.1.1.4"
    const val androidxAnnotation = "1.6.0"
}
