package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import me.him188.ani.app.platform.ContextMP

@Stable
expect class OAuthLauncherState {
    val onCodeCallback: (code: String, callbackUrl: String) -> Unit

    val isLaunching: Boolean
    val isLaunched: Boolean
    val isFailed: Boolean

    fun launch(context: ContextMP)
}

@Composable
expect fun rememberOAuthLauncherState(
    onCodeCallback: (code: String, callbackUrl: String) -> Unit
): OAuthLauncherState

