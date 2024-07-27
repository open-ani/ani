import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    sourceSets.commonMain.dependencies {
    }

    sourceSets.jvmMain.dependencies {
        api(libs.jetbrains.annotations)
    }

    sourceSets.nativeMain.dependencies {
        implementation(libs.kotlinx.datetime)
    }
}
