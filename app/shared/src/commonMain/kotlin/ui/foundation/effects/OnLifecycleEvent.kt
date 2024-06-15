package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleObserver
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.State> {
    val st = remember { mutableStateOf(currentState) }
    DisposableEffect(this) {
        val observer = object : LifecycleObserver {
            override fun onStateChanged(state: Lifecycle.State) {
                st.value = state
            }
        }
        this@observeAsState.addObserver(observer)
        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }
    return st
}

@Composable
fun OnLifecycleEvent(onEvent: (event: Lifecycle.State) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = object : LifecycleObserver {
            override fun onStateChanged(state: Lifecycle.State) {
                eventHandler.value(state)
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}
