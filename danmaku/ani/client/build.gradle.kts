plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(projects.danmaku.api)
    api(projects.danmaku.protocol)
    testImplementation(libs.kotlinx.coroutines.test)
}
