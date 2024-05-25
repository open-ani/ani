plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
    `flatten-source-sets`
}

dependencies {
    api(projects.dataSources.api)

    api(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)

    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jsoup)
    implementation(libs.slf4j.api)
    implementation(projects.utils.slf4jKt)
    implementation(projects.utils.ktorClient)
    runtimeOnly(libs.ktor.client.okhttp)
    implementation(libs.slf4j.simple)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
