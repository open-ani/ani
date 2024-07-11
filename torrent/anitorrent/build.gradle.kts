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
    idea
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

val configureAnitorrent = tasks.register("configureAnitorrent", Exec::class.java) {
    group = "anitorrent"
    dependsOn(generateSwig)
    // /Users/him188/Applications/CLion.app/Contents/bin/cmake/mac/aarch64/bin/cmake -DCMAKE_BUILD_TYPE=Debug 
    // -DCMAKE_MAKE_PROGRAM=/Users/him188/Applications/CLion.app/Contents/bin/ninja/mac/aarch64/ninja 
    // -G Ninja -S /Users/him188/Projects/ani/torrent/anitorrent 
    // -B /Users/him188/Projects/ani/torrent/anitorrent/cmake-build-debug

    val cmake = getPropertyOrNull("CMAKE") ?: "cmake"
    val ninja = getPropertyOrNull("NINJA") ?: "ninja"

    // Prefer clang, as the CI is tested with Clang
    val compilerC = getPropertyOrNull("CMAKE_C_COMPILER") ?: kotlin.run {
        when (getOs()) {
            Os.Windows -> {
                null
            }

            Os.Unknown,
            Os.MacOS,
            Os.Linux -> {
                File("/usr/bin/clang").takeIf { it.exists() }
                    ?: File("/usr/bin/gcc").takeIf { it.exists() }
            }
        }?.absolutePath?.also {
            logger.info("Using C compiler: $it")
        }
    }
    val compilerCxx = getPropertyOrNull("CMAKE_CXX_COMPILER") ?: kotlin.run {
        when (getOs()) {
            Os.Windows -> {
                File("C:/Program Files/LLVM/bin/clang++.exe").takeIf { it.exists() }
                    ?: File("C:/Program Files/LLVM/bin/clang++.exe").takeIf { it.exists() }
            }

            Os.Unknown,
            Os.MacOS,
            Os.Linux -> {
                File("/usr/bin/clang++").takeIf { it.exists() }
                    ?: File("/usr/bin/g++").takeIf { it.exists() }
            }
        }?.absolutePath?.also {
            logger.info("Using CXX compiler: $it")
        }
    }
    val isWindows = getOs() == Os.Windows

    inputs.file(anitorrentRootDir.resolve("CMakeLists.txt"))
    outputs.dir(anitorrentBuildDir)

    fun String.sanitize(): String {
        return this.replace("\\", "/").trim()
    }

    val buildType = getPropertyOrNull("CMAKE_BUILD_TYPE") ?: "Debug"
    check(buildType == "Debug" || buildType == "Release" || buildType == "RelWithDebInfo" || buildType == "MinSizeRel") {
        "Invalid build type: '$buildType'. Supported: Debug, Release, RelWithDebInfo, MinSizeRel"
    }

    // Note: to build in release mode on Windows:
    // --config Release
    // See also https://github.com/arvidn/libtorrent/issues/5111#issuecomment-688540049
    commandLine = buildList {
        add(cmake)
        add("-DCMAKE_BUILD_TYPE=$buildType")
        add("-DCMAKE_C_FLAGS_RELEASE=-O3")
        if (isWindows) {
            add("-Dencryption=OFF")
        }
        getPropertyOrNull("Boost_INCLUDE_DIR")?.let { add("-DBoost_INCLUDE_DIR=${it.sanitize()}") }
        if (!isWindows) {
            compilerC?.let { add("-DCMAKE_C_COMPILER=${compilerC.sanitize()}") }
            compilerCxx?.let { add("-DCMAKE_CXX_COMPILER=${compilerCxx.sanitize()}") }
            add("-DCMAKE_MAKE_PROGRAM=${ninja.sanitize()}")
            add("-G")
            add("Ninja")
        } else {
            getPropertyOrNull("CMAKE_TOOLCHAIN_FILE")?.let { add("-DCMAKE_TOOLCHAIN_FILE=${it.sanitize()}") }
        }
        add("-S")
        add(anitorrentRootDir.absolutePath)
        add("-B")
        add(anitorrentBuildDir.absolutePath)

        if (isWindows) {
            add("-- /m")
        }
    }
    logger.warn(commandLine.joinToString(" "))
}


val buildAnitorrent = tasks.register("buildAnitorrent", Exec::class.java) {
    group = "anitorrent"
    dependsOn(configureAnitorrent)
    dependsOn(generateSwig)

    val cmake = getPropertyOrNull("CMAKE") ?: "cmake"
    val isWindows = getOs() == Os.Windows
    val buildType = getPropertyOrNull("CMAKE_BUILD_TYPE") ?: "Debug"

    inputs.file(anitorrentRootDir.resolve("CMakeLists.txt"))
    inputs.dir(anitorrentRootDir.resolve("include"))
    inputs.dir(anitorrentRootDir.resolve("src"))
    inputs.file(anitorrentRootDir.resolve("gen/cpp/anitorrent_wrap.cpp"))
    outputs.dir(anitorrentBuildDir)

    // /Users/him188/Applications/CLion.app/Contents/bin/cmake/mac/aarch64/bin/cmake 
    // --build /Users/him188/Projects/ani/torrent/anitorrent/cmake-build-debug --target anitorrent -j 10
    commandLine = listOf(
        cmake,
        "--build", anitorrentBuildDir.absolutePath,
        "--target", "anitorrent",
        *if (isWindows && buildType == "Release") arrayOf("--config", "Release") else emptyArray(),
        "-j", Runtime.getRuntime().availableProcessors().toString(),
    )
}


tasks.getByName("compileJava") {
    if (enableAnitorrent) {
        dependsOn(generateSwig)
    }
}

idea {
    module {
        excludeDirs.add(anitorrentBuildDir)
        excludeDirs.add(file("cmake-build-debug"))
        excludeDirs.add(file("cmake-build-release"))
    }
}