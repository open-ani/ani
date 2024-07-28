plugins {
    kotlin("multiplatform")
    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.ksoup)
        api(projects.utils.io)
    }

    sourceSets.jvmMain.dependencies {
//        api(libs.jsoup)
    }

    sourceSets.nativeMain.dependencies {
//        api(libs.ksoup)
    }
}
