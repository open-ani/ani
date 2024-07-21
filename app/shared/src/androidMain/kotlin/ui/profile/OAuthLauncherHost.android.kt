package me.him188.ani.app.ui.profile

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.session.BangumiAuthorizationConstants


enum class LaunchChromeStatus {
    LAUNCHING,
    LAUNCHED,
    FAILED,
}

@Stable
actual class OAuthLauncherState(
    actual val onCodeCallback: (code: String, callbackUrl: String) -> Unit,
) {
    var launchChromeStatus: LaunchChromeStatus by mutableStateOf(LaunchChromeStatus.LAUNCHED)

    actual fun launch(context: ContextMP) {
        val intent = CustomTabsIntent.Builder().build()
        runCatching {
            intent.launchUrl(
                context,
                Uri.parse(BangumiAuthorizationConstants.makeOAuthUrl(currentAniBuildConfig.bangumiOauthClientAppId)),
            )
            launchChromeStatus = LaunchChromeStatus.LAUNCHED
        }.onFailure {
            launchChromeStatus = LaunchChromeStatus.FAILED
        }
    }

    actual val isLaunching: Boolean by derivedStateOf {
        launchChromeStatus == LaunchChromeStatus.LAUNCHING
    }
    actual val isLaunched: Boolean by derivedStateOf {
        launchChromeStatus == LaunchChromeStatus.LAUNCHED
    }
    actual val isFailed: Boolean by derivedStateOf {
        launchChromeStatus == LaunchChromeStatus.FAILED
    }
}

@Composable
actual fun rememberOAuthLauncherState(onCodeCallback: (code: String, callbackUrl: String) -> Unit): OAuthLauncherState {
    return remember {
        OAuthLauncherState(onCodeCallback)
    }
}
