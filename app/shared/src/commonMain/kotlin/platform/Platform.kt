@file:Suppress("unused", "NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package me.him188.ani.app.platform

import androidx.compose.runtime.Stable
import kotlin.contracts.contract

typealias Platform = me.him188.ani.utils.platform.Platform

/**
 * 获取当前的平台. 在 Linux 上使用时会抛出 [UnsupportedOperationException].
 *
 * CI 会跑 Ubuntu test (比较快), 所以在 test 环境需要谨慎使用此 API.
 */
@Stable
inline val currentPlatform: Platform
    get() = me.him188.ani.utils.platform.currentPlatform

@Stable
inline val currentPlatformDesktop: me.him188.ani.utils.platform.Platform.Desktop
    get() = me.him188.ani.utils.platform.currentPlatformDesktop

typealias ArchFamily = me.him188.ani.utils.platform.ArchFamily
typealias Arch = me.him188.ani.utils.platform.Arch

@Stable
inline fun Platform.isAArch(): Boolean = this.arch.family == ArchFamily.AARCH

@Stable
inline fun Platform.is64bit(): Boolean = this.arch.addressSizeBits == 64

@Stable
inline fun Platform.isDesktop(): Boolean {
    contract { returns(true) implies (this@isDesktop is me.him188.ani.utils.platform.Platform.Desktop) }
    return this is me.him188.ani.utils.platform.Platform.Desktop
}

@Stable
inline fun Platform.isMobile(): Boolean {
    contract { returns(true) implies (this@isMobile is me.him188.ani.utils.platform.Platform.Mobile) }
    return this is me.him188.ani.utils.platform.Platform.Mobile
}

@Stable
inline fun Platform.isAndroid(): Boolean {
    contract { returns(true) implies (this@isAndroid is me.him188.ani.utils.platform.Platform.Android) }
    return this is me.him188.ani.utils.platform.Platform.Android
}
