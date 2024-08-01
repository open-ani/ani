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

            api(projects.utils.ktorClient)
            api(projects.utils.logging)
            api(projects.dataSources.api)
        }
    }
    sourceSets.commonTest {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
