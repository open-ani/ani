package me.him188.ani.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import me.him188.ani.app.platform.window.PlatformWindowMP
import me.him188.ani.utils.io.SystemPath
import kotlin.contracts.contract

actual val LocalContext: ProvidableCompositionLocal<Context> = compositionLocalOf {
    error("No Context provided")
}

actual abstract class Context

class IosContext(
    val files: ContextFiles
) : Context()

fun Context.asIosContext(): IosContext {
    contract { returns() implies (this@asIosContext is IosContext) }
    return this as IosContext
}

internal actual val Context.filesImpl: ContextFiles get() = files

class IosContextFiles(
    override val cacheDir: SystemPath,
    override val dataDir: SystemPath
) : ContextFiles

/**
 * 横屏模式. 横屏模式不一定是全屏.
 *
 * PC 一定处于横屏模式.
 *
 * @see isSystemInFullscreenImpl
 */
@Composable
actual fun isInLandscapeMode(): Boolean {
    return false // TODO("Not yet implemented")
}

actual fun Context.setRequestFullScreen(window: PlatformWindowMP, fullscreen: Boolean) {

}

/**
 * @see isInLandscapeMode
 */
@Composable
actual fun isSystemInFullscreenImpl(): Boolean {
    return false
}
