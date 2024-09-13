package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import me.him188.ani.app.navigation.LocalNavigator

fun interface BackDispatcher {
    /**
     * Call the most recently registered [BackHandler], if any.
     *
     * If no [BackHandler] has been registered, this will call [AniNavigator.popBackStack].
     */
    fun onBackPressed()
}

/**
 * 可模拟点击返回键
 */
object LocalBackHandler {
    /**
     * @see BackDispatcher
     */
    val current: BackDispatcher
        @Composable
        get() {
            val backPressed by rememberUpdatedState(LocalOnBackPressedDispatcherOwner.current)
            val navigator by rememberUpdatedState(LocalNavigator.current)
            return remember {
                BackDispatcher {
                    backPressed?.onBackPressedDispatcher?.onBackPressed()
                        ?: kotlin.run {
                            navigator.popBackStack()
                        }
                }
            }
        }
}
