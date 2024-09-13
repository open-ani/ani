plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")

    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")

    id("com.google.devtools.ksp")
    id("androidx.room")
    idea
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.app.shared.appPlatform)
        api(projects.app.shared.videoPlayer.videoPlayerApi)
        api(projects.app.shared.videoPlayer.torrentSource)
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.collections.immutable)
        implementation(libs.kotlinx.serialization.json)
        implementation(projects.utils.io)
        implementation(projects.utils.coroutines)
        api(projects.danmaku.danmakuUiConfig)
        api(projects.utils.xml)
        api(projects.client)
        api(projects.utils.ipParser)
        
        api(projects.torrent.torrentApi)
        api(projects.torrent.anitorrent)

        api(libs.datastore.core) // Data Persistence
        api(libs.datastore.preferences.core) // Preferences
        api(libs.androidx.room.runtime)
        api(libs.sqlite.bundled)

        api(projects.datasource.datasourceApi)
        api(projects.datasource.datasourceCore)
        api(projects.datasource.bangumi)
        api(projects.datasource.mikan)
        api(projects.danmaku.danmakuApi)
        api(projects.danmaku.dandanplay)

        implementation(libs.koin.core)
    }
    sourceSets.commonTest.dependencies {
        implementation(projects.utils.uiTesting)
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.ui.tooling)
    }
    sourceSets.nativeMain.dependencies {
        implementation(libs.stately.common) // fixes koin bug
        implementation(libs.kotlinx.serialization.json.io)
    }
}

android {
    namespace = "me.him188.ani.app.data"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspDesktop", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    if (enableIos) {
        add("kspIosArm64", libs.androidx.room.compiler)
        add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    }
    debugImplementation(libs.androidx.compose.ui.tooling)
}
