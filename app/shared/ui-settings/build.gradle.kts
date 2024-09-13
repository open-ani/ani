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
        api(projects.app.shared.uiFoundation)
        implementation(compose.components.resources)
        implementation(libs.reorderable)
    }
    sourceSets.commonTest.dependencies {
    }
    sourceSets.androidMain.dependencies {
    }
    sourceSets.desktopMain.dependencies {
        implementation(libs.filekit.core)
        implementation(libs.filekit.compose)
    }
}

android {
    namespace = "me.him188.ani.app.ui.settings"
}

compose.resources {
    packageOfResClass = "me.him188.ani.app.ui.settings"
}
