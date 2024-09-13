package me.him188.ani.app.platform

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
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
