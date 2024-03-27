package me.him188.ani.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner

fun interface BackDispatcher {
    /**
     * Call the most recently registered [BackHandler], if any.
     *
     * If no [BackHandler] has been registered, this will call [Navigator.goBack].
     */
    fun onBackPress()
}

object LocalBackHandler {
    /**
     * @see BackDispatcher
     */
    val current: BackDispatcher
        @Composable
        get() {
            val backPressed by rememberUpdatedState(LocalBackDispatcherOwner.current)
            val navigator by rememberUpdatedState(LocalNavigator.current)
            return remember {
                BackDispatcher {
                    backPressed?.backDispatcher?.onBackPress()
                        ?: kotlin.run {
                            navigator.navigator.goBack()
                        }
                }
            }
        }
}