package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import me.him188.ani.app.platform.navigation.OnBackPressedHandler

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val onBackUpdated by rememberUpdatedState(onBack)
    val enabledUpdated = rememberUpdatedState(enabled)
    val owner = LocalOnBackPressedDispatcherOwner.current ?: return
    DisposableEffect(true, owner) {
        val handler = object : OnBackPressedHandler {
            override val enabled: Boolean by enabledUpdated
            override fun onBack() {
                onBackUpdated()
            }
        }
        owner.onBackPressedDispatcher.registerHandler(handler)
        onDispose {
            owner.onBackPressedDispatcher.unregisterHandler(handler)
        }
    }
}
