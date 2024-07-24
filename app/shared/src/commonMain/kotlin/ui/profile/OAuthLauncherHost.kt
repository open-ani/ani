package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.platform.ContextMP

enum class LaunchBrowserStatus {
    LAUNCHING,
    LAUNCHED,
    FAILED,
}

@Stable
abstract class ExternalBrowserLauncherState {
    var status: LaunchBrowserStatus by mutableStateOf(LaunchBrowserStatus.LAUNCHED)

    abstract fun launch(url: String, context: ContextMP)

    val isLaunching: Boolean by derivedStateOf {
        status == LaunchBrowserStatus.LAUNCHING
    }
    val isLaunched: Boolean by derivedStateOf {
        status == LaunchBrowserStatus.LAUNCHED
    }
    val isFailed: Boolean by derivedStateOf {
        status == LaunchBrowserStatus.FAILED
    }
}


@Composable
expect fun rememberExternalBrowserLauncherState(): ExternalBrowserLauncherState

