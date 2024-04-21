plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    api(projects.utils.slf4jKt)
    
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.kompendium.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(project(":danmaku:protocol"))
    implementation(project(":app:shared"))

    testImplementation(libs.kotlinx.coroutines.test)

    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass.set("me.him188.ani.danmaku.server.ApplicationKt")
}