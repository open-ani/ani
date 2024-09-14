plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")

    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.utils.platform)
        implementation(libs.kotlinx.coroutines.core)
        api(projects.danmaku.danmakuApi)
        api(projects.danmaku.danmakuUiConfig)
        implementation(libs.kotlinx.collections.immutable)
    }
    sourceSets.commonTest.dependencies {
        implementation(projects.utils.uiTesting)
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.ui.tooling)
        implementation(libs.compose.material3.adaptive.core)
        // Preview only
    }
}

android {
    namespace = "me.him188.ani.danmaku.ui"
}
