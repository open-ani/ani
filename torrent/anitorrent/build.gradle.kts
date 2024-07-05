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

plugins {
    kotlin("jvm")
    java
}

sourceSets.main {
    java.setSrcDirs(listOf("gen/java"))
    kotlin.setSrcDirs(listOf<String>())
}

/// ANITORRENT

val anitorrentRootDir = projectDir
val anitorrentBuildDir = anitorrentRootDir.resolve("build-ci")

val generateSwig = tasks.register("generateSwig", Exec::class.java) {
    group = "anitorrent"

    val swig = System.getenv("SWIG") ?: "swig"
//    swig -java -c++ \
//    -o ./src/anitorrent_wrap.cpp \
//    -outdir ./java/me/him188/ani/app/torrent/anitorrent/binding \
//    -package me.him188.ani.app.torrent.anitorrent.binding \
//            ./anitorrent.i

    val swigI = anitorrentRootDir.resolve("anitorrent.i")
    inputs.file(swigI)
    inputs.dir(anitorrentRootDir.resolve("include"))
    outputs.file(anitorrentRootDir.resolve("gen/cpp/anitorrent_wrap.cpp"))
    outputs.dir(anitorrentRootDir.resolve("gen/java"))

    commandLine = listOf(
        swig,
        "-java", "-c++", "-directors", "-cppext", "cpp", "-addextern", 
        "-o", anitorrentRootDir.resolve("gen/cpp/anitorrent_wrap.cpp").absolutePath,
        "-outdir", anitorrentRootDir.resolve("gen/java/me/him188/ani/app/torrent/anitorrent/binding").absolutePath,
        "-package", "me.him188.ani.app.torrent.anitorrent.binding",
        swigI.absolutePath,
    )
}

val configureAnitorrentCppWrapper = tasks.register("configureAnitorrentCppWrapper", Exec::class.java) {
    group = "anitorrent"
    dependsOn(generateSwig)
    // /Users/him188/Applications/CLion.app/Contents/bin/cmake/mac/aarch64/bin/cmake -DCMAKE_BUILD_TYPE=Debug 
    // -DCMAKE_MAKE_PROGRAM=/Users/him188/Applications/CLion.app/Contents/bin/ninja/mac/aarch64/ninja 
    // -G Ninja -S /Users/him188/Projects/ani/torrent/anitorrent 
    // -B /Users/him188/Projects/ani/torrent/anitorrent/cmake-build-debug

    val cmake = System.getenv("CMAKE") ?: "cmake"
    val ninja = System.getenv("NINJA") ?: "ninja"

    inputs.file(anitorrentRootDir.resolve("CMakeLists.txt"))
    outputs.dir(anitorrentBuildDir)

    commandLine = listOf(
        cmake,
        "-DCMAKE_BUILD_TYPE=Debug",
        "-DCMAKE_MAKE_PROGRAM=$ninja",
        "-G", "Ninja",
        "-S", anitorrentRootDir.absolutePath,
        "-B", anitorrentBuildDir.absolutePath,
    )
}


val buildAnitorrentCppWrapper = tasks.register("buildAnitorrentCppWrapper", Exec::class.java) {
    group = "anitorrent"
    dependsOn(configureAnitorrentCppWrapper)
    dependsOn(generateSwig)

    val cmake = System.getenv("CMAKE") ?: "cmake"

    inputs.file(anitorrentRootDir.resolve("CMakeLists.txt"))
    inputs.dir(anitorrentRootDir.resolve("src"))
    inputs.file(anitorrentRootDir.resolve("gen/cpp/anitorrent_wrap.cpp"))
    outputs.dir(anitorrentBuildDir)

    // /Users/him188/Applications/CLion.app/Contents/bin/cmake/mac/aarch64/bin/cmake 
    // --build /Users/him188/Projects/ani/torrent/anitorrent/cmake-build-debug --target anitorrent -j 10
    commandLine = listOf(
        cmake,
        "--build", anitorrentBuildDir.absolutePath,
        "--target", "anitorrent",
        "-j", Runtime.getRuntime().availableProcessors().toString(),
    )
}


tasks.getByName("compileJava") {
    dependsOn(generateSwig)
}
