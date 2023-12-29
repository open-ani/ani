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

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke

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

fun Project.configureFlattenSourceSets() {
    val flatten = extra.runCatching { get("flatten.sourceset") }.getOrNull()?.toString()?.toBoolean() ?: true
    if (!flatten) return
    sourceSets {
        findByName("main")?.apply {
            resources.setSrcDirs(listOf(projectDir.resolve("resources")))
            java.setSrcDirs(listOf(projectDir.resolve("src")))
        }
        findByName("test")?.apply {
            resources.setSrcDirs(listOf(projectDir.resolve("testResources")))
            java.setSrcDirs(listOf(projectDir.resolve("test")))
        }
    }
}
