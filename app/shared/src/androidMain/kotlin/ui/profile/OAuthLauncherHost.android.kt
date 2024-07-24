package me.him188.ani.app.ui.profile

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import me.him188.ani.app.platform.ContextMP


@Stable
class AndroidExternalBrowserLauncherState : ExternalBrowserLauncherState() {
    override fun launch(url: String, context: ContextMP) {
        val intent = CustomTabsIntent.Builder().build()
        runCatching {
            intent.launchUrl(
                context,
                Uri.parse(url),
            )
            status = LaunchBrowserStatus.LAUNCHED
        }.onFailure {
            status = LaunchBrowserStatus.FAILED
        }
    }
}

@Composable
actual fun rememberExternalBrowserLauncherState(): ExternalBrowserLauncherState {
    return remember {
        AndroidExternalBrowserLauncherState()
    }
}
