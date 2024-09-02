package me.him188.ani.app.platform.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Android port 到官方, skiko 自己实现
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

// 对应安卓的结构
expect object LocalOnBackPressedDispatcherOwner {
    val current: OnBackPressedDispatcherOwner?
        @Composable get

    infix fun provides(dispatcherOwner: OnBackPressedDispatcherOwner):
            ProvidedValue<OnBackPressedDispatcherOwner?>
}

expect interface OnBackPressedDispatcherOwner {
    val onBackPressedDispatcher: OnBackPressedDispatcher
}

expect class OnBackPressedDispatcher {
    fun onBackPressed()
}
