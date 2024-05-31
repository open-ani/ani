package org.myani.ani.ci

class IncrementalUpdateMetadataHelper(
    val os
) {


}


val hostOS: OS by lazy {
    when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> OS.WINDOWS
        Os.isFamily(Os.FAMILY_MAC) -> OS.MACOS
        Os.isFamily(Os.FAMILY_UNIX) -> OS.LINUX
        else -> error("Unsupported OS: ${System.getProperty("os.name")}")
    }
}

enum class OS(
    val isUnix: Boolean,
) {
    WINDOWS(false),
    MACOS(true),
    LINUX(true),
}


val hostArch: String by lazy {
    when (val arch = System.getProperty("os.arch")) {
        "x86_64" -> "x86_64"
        "amd64" -> "x86_64"
        "arm64" -> "aarch64"
        "aarch64" -> "aarch64"
        else -> error("Unsupported host architecture: $arch")
    }
}
