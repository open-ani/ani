@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("kotlinx-atomicfu")
}

@OptIn(ExperimentalComposeLibrary::class)
dependencies {
    implementation(compose.desktop.currentOs) {
        exclude(compose.material)
    }
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.material3)
    implementation(compose.runtime)
    implementation(compose.preview)

    testImplementation(compose.uiTestJUnit4)

    implementation(projects.animationGardenApi)
}

compose.desktop {
    application {
        mainClass = "me.him188.AnimationGardenDesktop"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = project.name
            packageVersion = project.version.toString()
        }
    }
}
