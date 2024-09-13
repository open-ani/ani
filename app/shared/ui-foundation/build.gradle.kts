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
        implementation(projects.app.shared.appData)
        implementation(projects.app.shared.appPlatform)
        implementation(projects.utils.platform)
        implementation(libs.kotlinx.coroutines.core)
        implementation(projects.danmaku.danmakuApi)
        implementation(libs.kotlinx.collections.immutable)
        implementation(libs.kotlinx.serialization.protobuf)
        implementation(projects.app.shared.placeholder)
        
        api(libs.coil.compose.core)
        api(libs.coil.svg)
        api(libs.coil.network.ktor2)

        implementation(compose.components.resources)
        api(libs.compose.lifecycle.viewmodel.compose)
        api(libs.compose.lifecycle.runtime.compose)
        api(libs.compose.navigation.compose)
        api(libs.compose.navigation.runtime)
        api(libs.compose.material3.adaptive.core.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        api(libs.compose.material3.adaptive.layout.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        api(libs.compose.material3.adaptive.navigation0.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }

        implementation(projects.utils.bbcode)
        implementation(libs.constraintlayout.compose)
        
        api(libs.koin.core)
    }
    sourceSets.commonTest.dependencies {
        implementation(projects.utils.uiTesting)
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.ui.tooling)
        implementation(libs.compose.material3.adaptive.core.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        // Preview only
    }
    sourceSets.desktopMain.dependencies {
        implementation(libs.jna)
        implementation(libs.jna.platform)
    }
}

android {
    namespace = "me.him188.ani.app.foundation"
}
