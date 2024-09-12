import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    sourceSets.commonMain.dependencies {
        implementation(libs.atomicfu)
        implementation(libs.kotlinx.collections.immutable)
    }

    sourceSets.getByName("jvmMain").dependencies {
        api(libs.jetbrains.annotations)
    }

    sourceSets.nativeMain.dependencies {
        implementation(libs.kotlinx.datetime)
    }
}

android {
    namespace = "me.him188.ani.app.platform"
}
