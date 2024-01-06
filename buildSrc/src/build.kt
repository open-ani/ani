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

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

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

val testOptInAnnotations = arrayOf(
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.time.ExperimentalTime",
    "io.ktor.util.KtorExperimentalAPI",
    "kotlin.io.path.ExperimentalPathApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.serialization.ExperimentalSerializationApi",
)

val optInAnnotations = arrayOf(
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "androidx.compose.foundation.layout.ExperimentalLayoutApi",
    "androidx.compose.foundation.ExperimentalFoundationApi"
)

val testLanguageFeatures: List<String> = listOf(
//    "ContextReceivers"
)

fun Project.configureKotlinOptIns() {
    val sourceSets = kotlinSourceSets ?: return
    sourceSets.all {
        configureKotlinOptIns()
    }

    for (name in testLanguageFeatures) {
        enableLanguageFeatureForTestSourceSets(name)
    }
}

fun KotlinSourceSet.configureKotlinOptIns() {
    languageSettings.progressiveMode = true
    optInAnnotations.forEach { a ->
        languageSettings.optIn(a)
    }
    if (name.contains("test", ignoreCase = true)) {
        testOptInAnnotations.forEach { a ->
            languageSettings.optIn(a)
        }
    }
}

fun Project.preConfigureJvmTarget() {
    val defaultVer = JavaVersion.VERSION_1_8

    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions.jvmTarget = defaultVer.toString()
    }

    tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = defaultVer.toString()
        targetCompatibility = defaultVer.toString()
    }
}

fun Project.configureJvmTarget() {
    val defaultVer = JavaVersion.VERSION_1_8

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = defaultVer
        targetCompatibility = defaultVer
    }

    allKotlinTargets().all {
        if (this !is KotlinJvmTarget) return@all
        this.testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }
}

fun Project.configureEncoding() {
    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF8"
    }
}

const val JUNIT_VERSION = "5.7.2"

fun Project.configureKotlinTestSettings() {
    tasks.withType(Test::class) {
        useJUnitPlatform()
    }
    val b = "Auto-set for project '${project.path}'. (configureKotlinTestSettings)"
    when {
        isKotlinJvmProject -> {
            dependencies {
                "testImplementation"(kotlin("test-junit5"))?.because(b)

                "testApi"("org.junit.jupiter:junit-jupiter-api:$JUNIT_VERSION")?.because(b)
                "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${JUNIT_VERSION}")?.because(b)
            }
        }

        isKotlinMpp -> {
            kotlinSourceSets?.all {
                val sourceSet = this

                val target = allKotlinTargets()
                    .find { it.name == sourceSet.name.substringBeforeLast("Main").substringBeforeLast("Test") }

                if (sourceSet.name.contains("test", ignoreCase = true)) {
                    if (isJvmFinalTarget(target)) {
                        // For android, this should be done differently. See Android.kt
                        sourceSet.configureJvmTest(b)
                    } else {
                        if (sourceSet.name == "commonTest") {
                            sourceSet.dependencies {
                                implementation(kotlin("test"))?.because(b)
                                implementation(kotlin("test-annotations-common"))?.because(b)
                            }
                        } else {
                            // can be an Android sourceSet
                            // Do not even add "kotlin-test" for Android sourceSets. IDEA can't resolve them on sync
                        }
                    }
                }
            }
        }
    }
}

private fun isJvmFinalTarget(target: KotlinTarget?) =
    target?.platformType == KotlinPlatformType.jvm

fun KotlinSourceSet.configureJvmTest(because: String) {
    dependencies {
        implementation(kotlin("test-junit5"))?.because(because)

        implementation("org.junit.jupiter:junit-jupiter-api:${JUNIT_VERSION}")?.because(because)
        runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${JUNIT_VERSION}")?.because(because)
    }
}

