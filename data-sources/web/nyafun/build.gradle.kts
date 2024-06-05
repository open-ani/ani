plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
    `flatten-source-sets`
}

dependencies {
    api(projects.dataSources.api)
    implementation(projects.dataSources.webBase)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
