package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls

@Composable
actual fun BackPressedHandler(enabled: Boolean, onBackPressed: @DisallowComposableCalls () -> Unit) {
    // no back pressed functionality on desktop
}

