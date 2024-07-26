package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.Composable

@Composable
fun ScreenRotationEffect(onChange: (Boolean) -> Unit) =
    ScreenRotationEffectImpl(onChange) // workaround for IDE completion bug

@Composable
expect fun ScreenRotationEffectImpl(onChange: (Boolean) -> Unit)
