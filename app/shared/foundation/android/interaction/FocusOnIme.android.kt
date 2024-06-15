package me.him188.ani.app.interaction

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable

@Composable
@Suppress("NOTHING_TO_INLINE")
actual inline fun isImeVisible(): Boolean = WindowInsets.isImeVisible