@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    // 注意! 前几个插件顺序非常重要, 调整后可能导致 compose multiplatform resources 生成错误
    `flatten-source-sets`

    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
}

extra.set("ani.jvm.target", 17)

kotlin {
    androidTarget()
    jvm("desktop")

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        // Workaround for MPP compose bug, don't change
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }
    }
}

compose.resources {
    packageOfResClass = "me.him188.ani.app.image.viewer"
    generateResClass = always
}

composeCompiler {
    enableStrongSkippingMode = true
}


kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.serialization.json)
        implementation(libs.atomicfu) // room runtime

        // Compose
        api(compose.foundation)
        api(compose.animation)
        api(compose.ui)
        api(compose.material3)
        api(compose.materialIconsExtended)
        api(compose.runtime)
        implementation(compose.components.resources)
    }

    sourceSets.androidMain.dependencies {
        api(libs.kotlinx.coroutines.android)

        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.compose.material3)
    }
    
}

// compose bug
tasks.named("generateComposeResClass") {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType(KotlinCompilationTask::class) {
    dependsOn("generateComposeResClass")
    dependsOn("generateResourceAccessorsForAndroidRelease")
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
    dependsOn("generateResourceAccessorsForAndroidUnitTestRelease")
    dependsOn("generateResourceAccessorsForAndroidUnitTestDebug")
    dependsOn("generateResourceAccessorsForAndroidDebug")
}

android {
    namespace = "me.him188.ani"
    compileSdk = getIntProperty("android.compile.sdk")
    defaultConfig {
        minSdk = getIntProperty("android.min.sdk")
    }
    buildTypes.getByName("release") {
        isMinifyEnabled = false // shared 不能 minify, 否则构建 app 会失败
        isShrinkResources = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            *sharedAndroidProguardRules(),
        )
    }
    buildFeatures {
        compose = true
    }
}
