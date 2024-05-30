plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    idea
    `flatten-source-sets`
}

sourceSets.main {
    kotlin.srcDir("gen")
}

idea {
    module {
        generatedSourceDirs.add(file("gen"))
    }
}

dependencies {
    api(projects.dataSources.api)
    implementation(projects.utils.serialization)
    implementation(projects.utils.slf4jKt)

    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.client.core)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jsoup)
    implementation(libs.slf4j.api)
    implementation(projects.utils.slf4jKt)

    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")


    testImplementation(libs.slf4j.simple)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
