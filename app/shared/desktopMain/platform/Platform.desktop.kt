package me.him188.ani.app.platform

actual fun Platform.Companion.currentPlatform(): Platform {
    System.getProperty("os.name")?.lowercase()?.let {
        when {
            it.contains("mac") || it.contains("os x") -> return Platform.Desktop("macOS")
            it.contains("windows") -> return Platform.Desktop("Windows")
            it.contains("linux") -> return Platform.Desktop("Linux")
            else -> return Platform.Desktop(it)
        }
    }

    return Platform.Desktop("Desktop")
}