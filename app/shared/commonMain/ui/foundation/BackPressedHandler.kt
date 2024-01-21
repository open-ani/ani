package me.him188.ani.app.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls

/**
 * Register a callback to be invoked when the user presses the back button.
 *
 * @param onBackPressed will be called on main thread.
 */
@Composable
expect fun BackPressedHandler(
    enabled: Boolean,
    onBackPressed: @DisallowComposableCalls () -> Unit
)

