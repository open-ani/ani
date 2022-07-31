@file:Suppress("OPT_IN_IS_NOT_ENABLED")

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.17.3")
    }
}

plugins {
    kotlin("jvm") apply false
    id("org.jetbrains.compose") apply false
}

allprojects {
    group = "me.him188"
    version = "1.0.0"

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}