package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.Composable

@Composable
fun ScreenRotationEffect(onChange: (isLandscape: Boolean) -> Unit) =
    ScreenRotationEffectImpl(onChange) // workaround for IDE completion bug

@Composable
expect fun ScreenRotationEffectImpl(onChange: (isLandscape: Boolean) -> Unit)
