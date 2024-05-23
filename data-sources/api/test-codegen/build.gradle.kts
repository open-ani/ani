plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `flatten-source-sets`
    idea
}

dependencies {
    implementation(projects.dataSources.api)
    implementation(projects.utils.ktorClient)
    implementation(projects.dataSources.dmhy)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.slf4j.simple)
    implementation(libs.jsoup)
    implementation(libs.kotlinpoet)
}

idea {
    module.excludeDirs.add(file("testData")) // avoid indexing large files
}