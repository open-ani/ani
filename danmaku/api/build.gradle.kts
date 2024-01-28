plugins {
    kotlin("jvm")
//    `android-library`
}

//kotlin {
//    androidTarget()
//    jvm("desktop")
//}

//configureFlattenMppSourceSets()
//configureAndroidLibrary("me.him188.ani.danmaku.api", libs.versions.jetpack.compose.compiler.get())

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
