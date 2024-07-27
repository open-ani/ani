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

    @Stable
    companion object {
        @Stable
        val currentPlatform: Platform = currentPlatformImpl()
    }
}

@Stable
val currentPlatform: Platform
    get() = Platform.currentPlatform

@Stable
val currentPlatformDesktop: Platform.Desktop
    get() {
        check(Platform.currentPlatform is Platform.Desktop)
        return Platform.currentPlatform
    }

@Immutable
enum class Arch(
    val displayName: String, // Don't change, used by the server
) {
    X86_64("x86_64"),
    AARCH64("aarch64"),
}

@Stable
expect fun Platform.Companion.currentPlatformImpl(): Platform

@Stable
fun Platform.isAarch64(): Boolean = this.arch == Arch.AARCH64

@Stable
fun Platform.isDesktop(): Boolean {
    contract { returns(true) implies (this@isDesktop is Platform.Desktop) }
    return this is Platform.Desktop
}

@Stable
fun Platform.isMobile(): Boolean {
    contract { returns(true) implies (this@isMobile is Platform.Mobile) }
    return this is Platform.Mobile
}

@Stable
fun Platform.isAndroid(): Boolean {
    contract { returns(true) implies (this@isAndroid is Platform.Android) }
    return this is Platform.Android
}
