package me.him188.ani.utils.platform

import android.os.Build

internal actual fun Platform.Companion.currentPlatformImpl(): Platform {
    return Build.SUPPORTED_ABIS.getOrNull(0)?.let { abi ->
        when (abi.lowercase()) {
            "armeabi-v7a" -> Platform.Android(Arch.ARMV7A)
            "arm64-v8a" -> Platform.Android(Arch.ARMV8A)
            "x86_64" -> Platform.Android(Arch.X86_64)
            else -> Platform.Android(Arch.ARMV8A)
        }
    } ?: Platform.Android(Arch.ARMV8A)
}
