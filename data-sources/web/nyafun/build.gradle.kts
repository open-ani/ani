plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
    `flatten-source-sets`
}

dependencies {
    api(projects.datasource.datasourceApi)
    implementation(projects.datasource.webBase)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
