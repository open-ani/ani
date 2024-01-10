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

import com.android.build.api.dsl.CommonExtension
import kotlinx.atomicfu.plugin.gradle.withKotlinTargets
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File

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

/**
 * 扁平化源集目录结构, 减少文件树层级 by 2
 *
 * 变化:
 * ```
 * src/${targetName}Main/kotlin -> ${targetName}Main
 * src/${targetName}Main/resources -> ${targetName}Resources
 * src/${targetName}Test/kotlin -> ${targetName}Test
 * src/${targetName}Test/resources -> ${targetName}TestResources
 * ```
 *
 * `${targetName}` 可以是 `common`, `android` `desktop` 等.
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

/**
 * 扁平化多平台项目的源集目录结构, 减少文件树层级 by 2
 *
 * 变化:
 * ```
 * src/androidMain/res -> androidRes
 * src/androidMain/assets -> androidAssets
 * src/androidMain/aidl -> androidAidl
 * src/${targetName}Main/kotlin -> ${targetName}Main
 * src/${targetName}Main/resources -> ${targetName}Resources
 * src/${targetName}Test/kotlin -> ${targetName}Test
 * src/${targetName}Test/resources -> ${targetName}TestResources
 * ```
 *
 * `${targetName}` 可以是 `common`, `android` `desktop` 等.
 */
fun Project.configureFlattenMppSourceSets() {
    kotlinSourceSets?.invoke {
        fun setForTarget(
            targetName: String,
        ) {
            findByName("${targetName}Main")?.apply {
                resources.setSrcDirs(listOf(projectDir.resolve("${targetName}Resources")))
                kotlin.setSrcDirs(listOf(projectDir.resolve("${targetName}Main")))
            }
            findByName("${targetName}Test")?.apply {
                resources.setSrcDirs(listOf(projectDir.resolve("${targetName}TestResources")))
                kotlin.setSrcDirs(listOf(projectDir.resolve("${targetName}Test")))
            }
        }

        setForTarget("common")

        allKotlinTargets().all {
            val targetName = name
            setForTarget(targetName)
        }
    }

    extensions.configure(CommonExtension::class) {
        this.sourceSets["main"].res.srcDirs(projectDir.resolve("androidRes"))
        this.sourceSets["main"].assets.srcDirs(projectDir.resolve("androidAssets"))
        this.sourceSets["main"].aidl.srcDirs(projectDir.resolve("androidAidl"))
    }
}

fun Project.sharedAndroidProguardRules(): Array<File> {
    return arrayOf(file(project(":app:shared").projectDir.resolve("proguard-rules.pro")))
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
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "androidx.compose.material3.ExperimentalMaterial3Api",
    "androidx.compose.ui.ExperimentalComposeUiApi",
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

val DEFAULT_JVM_TARGET = JavaVersion.VERSION_11


private fun Project.getProjectPreferredJvmTargetVersion() = extra.runCatching { get("ani.jvm.target") }.fold(
    onSuccess = { JavaVersion.toVersion(it.toString()) },
    onFailure = { DEFAULT_JVM_TARGET }
)

fun Project.configureJvmTarget() {
    val ver = getProjectPreferredJvmTargetVersion()
    logger.info("JVM target for project ${this.path} is: $ver")

    // 我也不知道到底设置谁就够了, 反正全都设置了

    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions.jvmTarget = ver.toString()
    }

    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions.jvmTarget = ver.toString()
    }

    tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = ver.toString()
        targetCompatibility = ver.toString()
    }

    extensions.findByType(KotlinProjectExtension::class)?.apply {
        jvmToolchain(ver.getMajorVersion().toInt())
    }

    extensions.findByType(JavaPluginExtension::class)?.apply {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
            sourceCompatibility = ver
            targetCompatibility = ver
        }
    }

    withKotlinTargets {
        it.compilations.all {
            if (this is KotlinJvmAndroidCompilation) {
                kotlinOptions.jvmTarget = ver.toString()
            }
        }
    }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = ver
        targetCompatibility = ver
    }

    extensions.findByType(CommonExtension::class)?.apply {
        compileOptions {
            sourceCompatibility = ver
            targetCompatibility = ver
        }
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

    allKotlinTargets().all {
        if (this !is KotlinJvmTarget) return@all
        this.testRuns["test"].executionTask.configure { useJUnitPlatform() }
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

