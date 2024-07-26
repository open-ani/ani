package me.him188.ani.app.ui.foundation.effects

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


@Composable
actual fun ScreenRotationEffectImpl(onChange: (isLandscape: Boolean) -> Unit) {
    onChange(LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
}