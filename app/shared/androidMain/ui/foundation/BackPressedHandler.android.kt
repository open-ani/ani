package me.him188.ani.app.ui.foundation

import android.os.Build
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import me.him188.ani.app.platform.LocalContext

@Composable
actual fun BackPressedHandler(
    enabled: Boolean,
    onBackPressed: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        BackPressedHandlerTiramisu(enabled, onBackPressed)
    } else {
        LaunchedEffect(key1 = true) {
            Log.e("BackInvokeHandler", "Not supported on this platform")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun BackPressedHandlerTiramisu(
    enabled: Boolean,
    callback: () -> Unit
) {
    val enabledState by rememberUpdatedState(newValue = enabled)
    val callbackState by rememberUpdatedState(newValue = callback)
    val onBackPressedDispatcherOwner = LocalContext.current as OnBackPressedDispatcherOwner
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    // If activity or lifecycle change, re-register
    if (enabled) {
        DisposableEffect(onBackPressedDispatcherOwner, lifecycleOwner.lifecycle) {
            val theCallback = object : OnBackPressedCallback(true) {
                @MainThread
                override fun handleOnBackPressed() {
                    if (enabledState) {
                        callbackState()
                    }
                }
            }

            onBackPressedDispatcherOwner.onBackPressedDispatcher.addCallback(lifecycleOwner, theCallback)

            onDispose {
                theCallback.remove()
            }
        }
    }
}


