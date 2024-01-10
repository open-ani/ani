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

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectList
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget


inline fun <reified T> Any?.safeAs(): T? {
    return this as? T
}

val Project.kotlinSourceSets get() = extensions.findByName("kotlin").safeAs<KotlinProjectExtension>()?.sourceSets

fun Project.allKotlinTargets(): NamedDomainObjectCollection<KotlinTarget> {
    return extensions.findByName("kotlin")?.safeAs<KotlinSingleTargetExtension<*>>()
        ?.target?.let { namedDomainObjectListOf(it) }
        ?: extensions.findByName("kotlin")?.safeAs<KotlinMultiplatformExtension>()?.targets
        ?: namedDomainObjectListOf()
}

private inline fun <reified T> Project.namedDomainObjectListOf(vararg values: T): NamedDomainObjectList<T> {
    return objects.namedDomainObjectList(T::class.java).apply { addAll(values) }
}

val Project.isKotlinJvmProject: Boolean get() = extensions.findByName("kotlin") is KotlinJvmProjectExtension
val Project.isKotlinMpp: Boolean get() = extensions.findByName("kotlin") is KotlinMultiplatformExtension

fun Project.allKotlinCompilations(action: (KotlinCompilation<KotlinCommonOptions>) -> Unit) {
    allKotlinTargets().all {
        compilations.all(action)
    }
}

inline fun org.gradle.api.NamedDomainObjectProvider<KotlinSourceSet>.dependencies(crossinline block: context(KotlinSourceSet) KotlinDependencyHandler.() -> Unit) {
    configure {
        dependencies {
            block(this)
        }
    }
}