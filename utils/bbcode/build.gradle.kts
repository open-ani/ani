import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask

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
    `ani-mpp-lib-targets`
    id("com.strumenta.antlr-kotlin")
    idea
}

val generatedRoot = layout.buildDirectory.dir("gen").get().asFile

kotlin {
    sourceSets.commonMain {
        dependencies {
            // antlr kotlin
            implementation(libs.antlr.kotlin.runtime)
        }
        kotlin.srcDirs(generatedRoot)
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedRoot)
    }
}

val generateBBCodeGrammarSource = tasks.register<AntlrKotlinTask>("generateBBCodeGrammarSource") {
    dependsOn(cleanBBCodeGrammarSource)

    source = fileTree(layout.projectDirectory) {
        include("BBCode.g4")
    }

    packageName = "me.him188.ani.utils.bbcode"
    arguments = listOf("-visitor")

    outputDirectory = generatedRoot
}

val cleanBBCodeGrammarSource = tasks.register<Delete>("cleanBBCodeGrammarSource") {
    delete(generatedRoot)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool> {
    dependsOn(generateBBCodeGrammarSource)
}