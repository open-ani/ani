package me.him188.ani.app.ui.foundation.effects

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.layout.hideBars
import me.him188.ani.app.ui.foundation.layout.showBars


@Composable
actual fun ScreenRotationEffectImpl(onChange: (isLandscape: Boolean) -> Unit) {
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (context is Activity) {
        if (isLandscape) {
            context.hideBars()
        } else {
            context.showBars()
        }
    }
    onChange(isLandscape)
}