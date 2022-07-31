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
}

kotlin {
    explicitApi()

    target.compilations.all {
        languageSettings.progressiveMode = true
    }
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}