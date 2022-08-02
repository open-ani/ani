plugins {
    kotlin("jvm")
    id("kotlinx-atomicfu")
}

dependencies {
    val ktorVersion = "2.0.3"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.2")
    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.15.2")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(kotlin("test-junit5"))
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

kotlin {
    sourceSets.all {
        languageSettings.progressiveMode = true
    }
}

tasks.test {
    useJUnitPlatform()
}