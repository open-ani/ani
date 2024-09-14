/*
 * Copyright 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.util.fastLastOrNull

actual object LocalOnBackPressedDispatcherOwner {
    private val LocalOnBackPressedDispatcherOwner =
        staticCompositionLocalOf<OnBackPressedDispatcherOwner?> { null }

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
