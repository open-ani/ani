plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")

    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(libs.kotlinx.serialization.core)
        }
    }
    sourceSets.commonTest {
        dependencies {
        }
    }
}

android {
    namespace = "me.him188.ani.danmaku.ui.config"
}
