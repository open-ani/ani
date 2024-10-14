/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File

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
    "me.him188.ani.utils.platform.annotations.TestOnly",
    "androidx.compose.ui.test.ExperimentalTestApi",
)

val optInAnnotations = arrayOf(
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.coroutines.FlowPreview",
    "androidx.compose.foundation.layout.ExperimentalLayoutApi",
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "androidx.compose.material3.ExperimentalMaterial3Api",
    "androidx.compose.ui.ExperimentalComposeUiApi",
    "org.jetbrains.compose.resources.ExperimentalResourceApi",
    "kotlin.ExperimentalStdlibApi",
    "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    "androidx.compose.animation.ExperimentalSharedTransitionApi",
)

val testLanguageFeatures: List<String> = listOf(
//    "ContextReceivers" // causes segfault on ios
)

fun Project.configureKotlinOptIns() {
    val sourceSets = kotlinSourceSets ?: return
    sourceSets.all {
        configureKotlinOptIns()
    }

    val libs = versionCatalogLibs()
    val (major, minor) = libs["kotlin"].split('.')
    val kotlinVersion = KotlinVersion.valueOf("KOTLIN_${major}_${minor}")

    val options = kotlinCommonCompilerOptions()
    options.apply {
        languageVersion.set(kotlinVersion)
    }
    // ksp task extends KotlinCompile
    project.tasks.withType(KotlinCompile::class.java) {
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
        compilerOptions.languageVersion.set(kotlinVersion)
    }

    for (name in testLanguageFeatures) {
        enableLanguageFeatureForTestSourceSets(name)
    }
}

private fun Project.versionCatalogLibs(): VersionCatalog =
    project.extensions.getByType<VersionCatalogsExtension>().named("libs")

private operator fun VersionCatalog.get(name: String): String = findVersion(name).get().displayName

private fun Project.kotlinCommonCompilerOptions(): KotlinCommonCompilerOptions = when (val ext = kotlinExtension) {
    is KotlinJvmProjectExtension -> ext.compilerOptions
    is KotlinAndroidProjectExtension -> ext.compilerOptions
    is KotlinMultiplatformExtension -> ext.compilerOptions
    else -> error("Unsupported kotlinExtension: ${ext::class}")
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

val Project.DEFAULT_JVM_TOOLCHAIN_VENDOR
    get() = getPropertyOrNull("jvm.toolchain.vendor")?.let { JvmVendorSpec.matching(it) }

private fun Project.getProjectPreferredJvmTargetVersion() = extra.runCatching { get("ani.jvm.target") }.fold(
    onSuccess = { JavaVersion.toVersion(it.toString()) },
    onFailure = { JavaVersion.toVersion(getPropertyOrNull("jvm.toolchain.version")?.toInt() ?: 17) },
)

fun Project.configureJvmTarget() {
    val ver = getProjectPreferredJvmTargetVersion()
    logger.info("JVM target for project ${this.path} is: $ver")

    // 我也不知道到底设置谁就够了, 反正全都设置了

    tasks.withType(KotlinJvmCompile::class.java) {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(ver.toString()))
    }

    tasks.withType(KotlinCompile::class.java) {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(ver.toString()))
    }

    tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = ver.toString()
        targetCompatibility = ver.toString()
    }

    extensions.findByType(KotlinProjectExtension::class)?.apply {
        jvmToolchain {
            vendor.set(DEFAULT_JVM_TOOLCHAIN_VENDOR)
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
        }
    }

    extensions.findByType(JavaPluginExtension::class)?.apply {
        toolchain {
            vendor.set(DEFAULT_JVM_TOOLCHAIN_VENDOR)
            languageVersion.set(JavaLanguageVersion.of(ver.getMajorVersion()))
            sourceCompatibility = ver
            targetCompatibility = ver
        }
    }

    withKotlinTargets {
        it.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
                }
            }
            if (this is KotlinJvmAndroidCompilation) {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.fromTarget(ver.toString()))
                    }
                }
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
                    when {
                        target?.platformType == KotlinPlatformType.jvm -> {
                            // For android, this should be done differently. See Android.kt
                            sourceSet.configureJvmTest(b)
                        }

                        sourceSet.name == "commonTest" -> {
                            sourceSet.dependencies {
                                implementation(kotlin("test"))?.because(b)
                                implementation(kotlin("test-annotations-common"))?.because(b)
                            }
                        }

                        target?.platformType == KotlinPlatformType.androidJvm -> {
                            // Android uses JUnit4
                            sourceSet.dependencies {
                                implementation("junit:junit:4.13")?.because(b)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun KotlinSourceSet.configureJvmTest(because: String) {
    dependencies {
        implementation(kotlin("test-junit5"))?.because(because)

        implementation("org.junit.jupiter:junit-jupiter-api:${JUNIT_VERSION}")?.because(because)
        runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${JUNIT_VERSION}")?.because(because)
    }
}


fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
        // find all compilations given sourceSet belongs to
        kotlinExtension.targets
            .all {
                fn(this)
            }
    }
}