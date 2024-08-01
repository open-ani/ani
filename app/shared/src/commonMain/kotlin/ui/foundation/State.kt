package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.StateFactoryMarker

private class ImmutableState<T>(
    override val value: T
) : State<T>

/**
 * 创建一个不可变的 [State]. 用于将一个值包装为 [State].
 */
@StateFactoryMarker
fun <T> stateOf(value: T): State<T> = ImmutableState(value)
