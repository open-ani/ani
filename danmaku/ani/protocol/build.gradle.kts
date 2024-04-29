plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `flatten-source-sets`
}

dependencies {
    api(libs.kotlinx.serialization.json)
}