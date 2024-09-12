plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")

    `ani-mpp-lib-targets`
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.utils.platform)
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.coroutines.test)
    }
}

android {
    namespace = "me.him188.ani.utils.ui.testing"
}
