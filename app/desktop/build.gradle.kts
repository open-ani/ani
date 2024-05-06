/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    `flatten-source-sets`
    id("kotlinx-atomicfu")
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.libtorrent4j.macos)
    implementation(compose.components.resources)
}

// workaround for compose limitation
tasks.named("processResources") {
    dependsOn(":app:shared:desktopProcessResources")
}

sourceSets {
    main {
        resources.srcDirs(
            project(":app:shared").layout.buildDirectory
//                .file("generated/compose/resourceGenerator/preparedResources/commonMain")
                .file("processedResources/desktop/main")
        )
    }
}

extra.set("ani.jvm.target", 17)

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "me.him188.ani.desktop.AniDesktop"
//        jvmArgs("--add-exports=java.desktop/com.apple.eawt=ALL-UNNAMED")
        nativeDistributions {
            targetFormats(*buildList {
                add(TargetFormat.Deb)
                add(TargetFormat.Rpm)
                add(TargetFormat.Dmg)
//                if (getOs() == Os.Windows) {
//                    add(TargetFormat.AppImage) // portable distribution (installation-free)
//                }
            }.toTypedArray())
            packageName = "Ani"
            description = project.description
            vendor = "Him188"
            // adding copyright causes package to fail.
//            copyright = """
//                    Ani
//                    Copyright (C) 2022-2024 Him188
//
//                    This program is free software: you can redistribute it and/or modify
//                    it under the terms of the GNU General Public License as published by
//                    the Free Software Foundation, either version 3 of the License, or
//                    (at your option) any later version.
//
//                    This program is distributed in the hope that it will be useful,
//                    but WITHOUT ANY WARRANTY; without even the implied warranty of
//                    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//                    GNU General Public License for more details.
//
//                    You should have received a copy of the GNU General Public License
//                    along with this program.  If not, see <https://www.gnu.org/licenses/>.
//            """.trimIndent()
            licenseFile.set(rootProject.rootDir.resolve("LICENSE"))
            packageVersion = properties["package.version"].toString()
        }

        // TODO: Uncomment this to enable proguard for desktop, need some tweaks
//        buildTypes.release.proguard {
//            this.configurationFiles.from(project(":app:shared").file("proguard-rules.pro"))
//            this.configurationFiles.from(file("proguard-desktop.pro"))
//        }
    }
}

// workaround for resource not found
//kotlin.sourceSets.main.get().resources.srcDir(project(":common").projectDir.resolve("src/androidMain/res/raw"))
