package me.him188.ani.app.platform.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.util.fastLastOrNull

actual object LocalOnBackPressedDispatcherOwner {
    private val LocalOnBackPressedDispatcherOwner =
        compositionLocalOf<OnBackPressedDispatcherOwner?> { null }

    /**
     * Returns current composition local value for the owner or `null` if one has not
     * been provided, one has not been set via
     * [androidx.activity.setViewTreeOnBackPressedDispatcherOwner], nor is one available by
     * looking at the [LocalContext].
     */
    actual val current: OnBackPressedDispatcherOwner?
        @Composable
        get() = LocalOnBackPressedDispatcherOwner.current

    /**
     * Associates a [LocalOnBackPressedDispatcherOwner] key to a value in a call to
     * [CompositionLocalProvider].
     */
    actual infix fun provides(dispatcherOwner: OnBackPressedDispatcherOwner):
            ProvidedValue<OnBackPressedDispatcherOwner?> {
        return LocalOnBackPressedDispatcherOwner.provides(dispatcherOwner)
    }
}

actual interface OnBackPressedDispatcherOwner {
    actual val onBackPressedDispatcher: OnBackPressedDispatcher
}

actual class OnBackPressedDispatcher(
    private val fallback: () -> Unit,
) {
    private val handlers = mutableListOf<OnBackPressedHandler>()
    actual fun onBackPressed() {
        handlers.fastLastOrNull { it.enabled }?.onBack() ?: fallback()
    }

    fun registerHandler(handler: OnBackPressedHandler) {
        this.handlers.add(handler)
    }

    fun unregisterHandler(handler: OnBackPressedHandler) {
        this.handlers.remove(handler)
    }
}

interface OnBackPressedHandler {
    val enabled: Boolean
    fun onBack()
}
