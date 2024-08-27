package me.him188.ani.app.ui.foundation.effects

import androidx.compose.ui.Modifier

/**
 * 仅在 PC 有效, 当 [visible] 为 false 时, 隐藏光标
 */
expect fun Modifier.cursorVisibility(visible: Boolean = true): Modifier
