package me.him188.ani.app.platform

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.contracts.contract

@Immutable
sealed class Platform {
    abstract val name: String // don't change, it's actually an ID
    abstract val arch: Arch

    val nameAndArch get() = "$name ${arch.displayName}"
    final override fun toString(): String = nameAndArch

    ///////////////////////////////////////////////////////////////////////////
    // mobile
    ///////////////////////////////////////////////////////////////////////////

    sealed class Mobile : Platform()

    data object Android : Mobile() {
        override val name: String get() = "Android"
        override val arch: Arch get() = Arch.AARCH64
    }

//    data object Ios : Platform() {
//        override val name: String get() = "iOS"
//        override val arch: Arch get() = Arch.AARCH64
//    }


    ///////////////////////////////////////////////////////////////////////////
    // desktop
    ///////////////////////////////////////////////////////////////////////////

    sealed class Desktop(
        override val name: String
    ) : Platform()

    data class Windows(
        override val arch: Arch
    ) : Desktop("Windows")

    data class MacOS(
        override val arch: Arch
    ) : Desktop("macOS")

    data class Linux(
        override val arch: Arch
    ) : Desktop("Linux")

    companion object {
        @Stable
        val currentPlatform: Platform = currentPlatformImpl()
    }
}

enum class Arch(
    val displayName: String, // Don't change, used by the server
) {
    X86_64("x86_64"),
    AARCH64("aarch64"),
}

expect fun Platform.Companion.currentPlatformImpl(): Platform

fun Platform.isAarch64(): Boolean = this.arch == Arch.AARCH64

fun Platform.isDesktop(): Boolean {
    contract { returns(true) implies (this@isDesktop is Platform.Desktop) }
    return this is Platform.Desktop
}

fun Platform.isMobile(): Boolean {
    contract { returns(true) implies (this@isMobile is Platform.Mobile) }
    return this is Platform.Mobile
}

fun Platform.isAndroid(): Boolean {
    contract { returns(true) implies (this@isAndroid is Platform.Android) }
    return this is Platform.Android
}
