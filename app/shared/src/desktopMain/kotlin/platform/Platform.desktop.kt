package me.him188.ani.app.platform

import androidx.compose.runtime.Stable

@Stable
val currentDesktopPlatform: Platform.Desktop
    get() = currentPlatform as Platform.Desktop

@Stable
actual fun Platform.Companion.currentPlatformImpl(): Platform {
    val os = System.getProperty("os.name")?.lowercase() ?: error("Cannot determine platform, 'os.name' is null.")
    val arch = getArch()
    return when {
        "mac" in os || "os x" in os || "darwin" in os -> Platform.MacOS(arch)
        "windows" in os -> Platform.Windows(arch)
        "linux" in os || "redhat" in os || "debian" in os || "ubuntu" in os -> Platform.Linux(arch)
        else -> error("Unsupported platform: $os")
    }
}

private fun getArch() = System.getProperty("os.arch").lowercase().let {
    when {
        "x86" in it || "x64" in it || "amd64" in it -> Arch.X86_64
        "arm" in it || "aarch" in it -> Arch.AARCH64
        else -> Arch.X86_64
    }
}
