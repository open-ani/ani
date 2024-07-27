plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    `flatten-source-sets`
}

dependencies {
    api(projects.dataSources.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.jsoup)

    api(projects.utils.ktorClient)
    api(projects.utils.logging)

    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.logging)
    api(libs.ktor.serialization.kotlinx.json)
    testApi(libs.ktor.client.okhttp)
    testApi(libs.slf4j.simple)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
