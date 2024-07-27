plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.client.logging)
    api(libs.kotlinx.serialization.core)

    api(projects.utils.ktorClient)
    api(projects.utils.logging)
    api(projects.dataSources.api)

    testImplementation(libs.kotlinx.coroutines.test)
}
