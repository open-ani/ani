plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `ani-mpp-lib-targets`
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.core)

            api(projects.danmaku.danmakuApi)
            api(projects.utils.ktorClient)
            api(projects.utils.logging)
            api(projects.datasource.datasourceApi)
        }
    }
    sourceSets.commonTest {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
