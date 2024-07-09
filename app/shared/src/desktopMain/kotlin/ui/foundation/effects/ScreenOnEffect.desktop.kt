package me.him188.ani.app.ui.foundation.effects

import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentDesktopPlatform
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

private interface DesktopScreenOnEffect {
    fun preventSleep(): Boolean
    fun allowSleep()

    companion object {
        fun createForCurrentOS(): DesktopScreenOnEffect {
            return when (currentDesktopPlatform) {
                is Platform.Linux -> throw UnsupportedOperationException("Linux is not supported yet")
                is Platform.MacOS -> MacosScreenOnEffect()
                is Platform.Windows -> TODO()
            }
        }
    }
}

@Keep
@Suppress("ConstPropertyName")
private class MacosScreenOnEffect : DesktopScreenOnEffect { // ChatGPT 写的
    @Suppress("FunctionName")
    private interface IOKit : Library {
        companion object {
            val INSTANCE: IOKit? = kotlin.runCatching { Native.load("IOKit", IOKit::class.java) }.getOrNull()
            const val kIOPMAssertionTypeNoDisplaySleep = 0
            const val kIOPMAssertionLevelOn = 255
            const val kIOReturnSuccess = 0
        }

        fun IOPMAssertionCreateWithName(
            assertionType: Int,
            assertionLevel: Int,
            assertionName: Pointer,
            assertionID: PointerByReference
        ): Int

        fun IOPMAssertionRelease(assertionID: IOPMAssertionID): Int
    }

    @Suppress("FunctionName")
    private interface CoreFoundation : Library {
        companion object {
            val INSTANCE: CoreFoundation? =
                kotlin.runCatching { Native.load("CoreFoundation", CoreFoundation::class.java) }
                    .getOrNull()
        }

        fun CFStringCreateWithCString(
            allocator: Pointer?,
            cStr: String,
            encoding: Int
        ): Pointer
    }

    private class IOPMAssertionID(pointer: Pointer?) : PointerType(pointer)

    private var assertionID: IOPMAssertionID? = null

    override fun preventSleep(): Boolean {
        val assertionName = CoreFoundation.INSTANCE?.CFStringCreateWithCString(
            null, "Prevent sleep for Ani video player", kCFStringEncodingUTF8,
        ) ?: return false
        val assertionIDRef = PointerByReference()
        val result = IOKit.INSTANCE?.IOPMAssertionCreateWithName(
            IOKit.kIOPMAssertionTypeNoDisplaySleep,
            IOKit.kIOPMAssertionLevelOn,
            assertionName,
            assertionIDRef,
        ) ?: return false
        if (result == IOKit.kIOReturnSuccess) {
            assertionID = IOPMAssertionID(assertionIDRef.value)
            return true
        } else {
            return false
        }
    }

    override fun allowSleep() {
        assertionID?.let {
            IOKit.INSTANCE?.IOPMAssertionRelease(it)
            assertionID = null
        }
    }

    private companion object {
        private const val kCFStringEncodingUTF8 = 0x08000100
    }
}

private val logger = logger<DesktopScreenOnEffect>()

/**
 * Composes an effect that keeps the screen on.
 *
 * When the composable gets removed from the view hierarchy, the screen will be allowed to turn off again.
 */
@Composable
actual fun ScreenOnEffectImpl() {
    val effect = remember {
        DesktopScreenOnEffect.createForCurrentOS()
    }

    DisposableEffect(effect) {
        kotlin.runCatching {
            if (effect.preventSleep()) {
                logger.info { "Successfully set preventSleep" }
            } else {
                logger.info { "Failed to set preventSleep" }
            }
        }.onFailure {
            logger.error(it) { "Failed to set preventSleep" }
        }
        onDispose {
            kotlin.runCatching {
                effect.allowSleep()
            }.onFailure {
                logger.error(it) { "Failed to set allowSleep" }
            }
        }
    }
}
