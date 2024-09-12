plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    `flatten-source-sets`
}

dependencies {
    api(projects.datasource.datasourceApi)
    implementation(projects.datasource.webBase)
    testApi(libs.ktor.client.okhttp)
    testApi(libs.slf4j.simple)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
