package me.him188.ani.app.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import me.him188.ani.app.session.BangumiAuthorizationConstants
import me.him188.ani.app.session.BangumiOAuthCallbackServer
import me.him188.ani.app.ui.foundation.launchInBackground
import java.awt.Desktop
import java.net.URI

@Composable
actual fun BangumiOAuthRequest(vm: AuthViewModel, onFailed: (Throwable) -> Unit, modifier: Modifier) {

    val server = remember {
        BangumiOAuthCallbackServer {
            vm.setCode(it, getCallbackUrl())
        }
    }

    DisposableEffect(server) {
        onDispose {
            server.close()
        }
    }

    var serverStarted by remember { mutableStateOf(false) }

    DisposableEffect(true) {
        vm.launchInBackground(Dispatchers.IO) {
            server.start()
            serverStarted = true
        }

        onDispose {
            server.close()
        }
    }

    Box(Modifier.padding(all = 16.dp)) {
        if (!serverStarted) {
            Text("正在启动回调服务器...")
        } else {
            Text("请在浏览器中完成授权, 然后回到这里")

            LaunchedEffect(true) {
                Desktop.getDesktop()
                    .browse(URI.create(BangumiAuthorizationConstants.makeOAuthUrl(callbackUrl = server.getCallbackUrl())))
            }
        }
    }
}
