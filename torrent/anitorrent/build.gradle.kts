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
                File("C:/Program Files/LLVM/bin/clang.exe").takeIf { it.exists() }
                    ?: File("C:/Program Files/LLVM/bin/clang.exe").takeIf { it.exists() }
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

    // Note: to build in release mode on Windows:
    // --config Release
    // See also https://github.com/arvidn/libtorrent/issues/5111#issuecomment-688540049
    commandLine = listOfNotNull(
        cmake,
        "-DCMAKE_BUILD_TYPE=Debug",
//        if (isWindows) "-DCMAKE_CXX_COMPILER_FORCED=true" else null,
//        if (isWindows) "-DCMAKE_C_COMPILER_FORCED=true" else null,
//        "-DCMAKE_MAKE_PROGRAM=${ninja.sanitize()}",
        compilerC?.let { "-DCMAKE_C_COMPILER=${compilerC.sanitize()}" },
        compilerCxx?.let { "-DCMAKE_CXX_COMPILER=${compilerCxx.sanitize()}" },
        "-DCMAKE_C_FLAGS_RELEASE=-O3",
        getPropertyOrNull("Boost_INCLUDE_DIR")?.let { "-DBoost_INCLUDE_DIR=${it.sanitize()}" },
        getPropertyOrNull("CMAKE_TOOLCHAIN_FILE")?.let { "-DCMAKE_TOOLCHAIN_FILE=${it.sanitize()}" },
//        "-G", "Ninja",
        "-S", anitorrentRootDir.absolutePath,
        "-B", anitorrentBuildDir.absolutePath,
    )
    logger.warn(commandLine.joinToString(" "))
}


val buildAnitorrent = tasks.register("buildAnitorrent", Exec::class.java) {
    group = "anitorrent"
    dependsOn(configureAnitorrent)
    dependsOn(generateSwig)

    val cmake = System.getenv("CMAKE") ?: "cmake"

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
        "-j", Runtime.getRuntime().availableProcessors().toString(),
    )
}


tasks.getByName("compileJava") {
    dependsOn(generateSwig)
}
