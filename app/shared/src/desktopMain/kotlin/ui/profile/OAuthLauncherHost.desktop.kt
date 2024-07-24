package me.him188.ani.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import me.him188.ani.app.platform.ContextMP


@Stable
class DesktopExternalBrowserLauncherState : ExternalBrowserLauncherState() {
    override fun launch(url: String, context: ContextMP) {

    }
}

@Composable
actual fun rememberExternalBrowserLauncherState(): ExternalBrowserLauncherState {
    return remember {
        DesktopExternalBrowserLauncherState()
    }
}

//@Composable
// fun OAuthLauncherHost(state: OAuthLauncherState, modifier: Modifier) {
//    // 备注: 刻意关掉了 PC 登录, 因为 bgm 回调只支持固定地址, 而 PC 需要启动本地服务器 (随机端口)
//    // 你可能觉得 bgm OAuth 请求时可以传递回调地址, 实际上似乎 bgm 服务器有 bug, 假如用户没有登录, 
//    // 待用户登录完成后它服务器就会忘记那个回调地址, 于是(网页)报错回调地址找不到
//    return
//
//    val server = remember {
//        BangumiOAuthCallbackServer {
//            vm.setCode(it, getCallbackUrl())
//        }
//    }
//
//    DisposableEffect(server) {
//        onDispose {
//            server.close()
//        }
//    }
//
//    var serverStarted by remember { mutableStateOf(false) }
//
//    DisposableEffect(true) {
//        vm.launchInBackground(Dispatchers.IO) {
//            server.start()
//            serverStarted = true
//        }
//
//        onDispose {
//            server.close()
//        }
//    }
//
//    Box(Modifier.padding(all = 16.dp)) {
//        if (!serverStarted) {
//            Text("正在启动回调服务器...")
//        } else {
//            Text("请在浏览器中完成授权, 然后回到这里")
//
//            LaunchedEffect(true) {
//                Desktop.getDesktop()
//                    .browse(URI.create(BangumiAuthorizationConstants.makeOAuthUrl(callbackUrl = server.getCallbackUrl())))
//            }
//        }
//    }
//}
