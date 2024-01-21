package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.Composable

@Composable
actual fun BackPressedHandler(enabled: Boolean, onBackPressed: () -> Unit) {
    // no back pressed functionality on desktop
}

