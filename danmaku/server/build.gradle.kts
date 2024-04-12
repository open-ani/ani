plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(projects.utils.slf4jKt)
    
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    
    runtimeOnly(libs.slf4j.simple)
}