package me.him188.ani.app.ui.profile.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.navigation.BackDispatcher
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.LocalBackHandler
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.TopAppBarActionButton
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.foundation.feedback.ErrorDialogHost
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.profile.AuthViewModel
import me.him188.ani.app.ui.profile.BangumiAuthIntroLayout
import me.him188.ani.app.ui.profile.BangumiOAuthRequest
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import org.koin.core.context.GlobalContext


@Composable
fun AuthRequestPage(
    vm: AuthViewModel,
    allowBack: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val nav = LocalNavigator.current
    BackHandler {
        vm.onCancel()
        nav.navigator.goBack()
    }
    Scaffold(
        modifier
            .systemBarsPadding()
            .fillMaxSize(),
        topBar = {
            val navigator = LocalBackDispatcherOwner.current
            AniTopAppBar(
                actions = {
                    if (allowBack) {
                        TopAppBarGoBackButton { navigator?.backDispatcher?.onBackPress() }
                    }
                    TopAppBarActionButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                },
                title = { Text(text = "登录 Bangumi") }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            AuthResults(vm)

            var confirmIntro by rememberSaveable { mutableStateOf(false) }

            val needAuth by vm.needAuth.collectAsStateWithLifecycle()
            if (needAuth) {
                if (confirmIntro) {
                    key(vm.retryCount.value) {
                        BangumiOAuthRequest(
                            vm,
                            Modifier.fillMaxSize()
                        )
                    }
                } else {
                    BasicAlertDialog(
                        { confirmIntro = true },
                        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                    ) {
                        BangumiAuthIntroLayout(onDismissRequest = { confirmIntro = true })
                    }
                }
            } else {
                // already logged in
                val backDispatcherOwner = LocalBackDispatcherOwner.current
                SideEffect {
                    backDispatcherOwner?.backDispatcher?.onBackPress()
                }
            }
        }
    }
}

@Composable
private fun AuthResults(viewModel: AuthViewModel) {
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    if (isProcessing != null) {
        val backHandler = LocalBackHandler.current
        BasicAlertDialog({}) {
            AuthDialogContent(viewModel, backHandler)
        }
    }

    ErrorDialogHost(
        viewModel.authError,
        onConfirm = {
            viewModel.dismissError()
        },
    )
}

@Stable
internal val Platform.supportsCallbackLogin: Boolean
    get() = when (this) {
        is Platform.Desktop -> false
        Platform.Android -> true
    }

@Composable
internal fun AuthDialogContent(
    viewModel: AuthViewModel,
    backHandler: BackDispatcher
) {
    var token by rememberSaveable { mutableStateOf("") }
    RichDialogLayout(
        title = {
            Text("Bangumi 授权")
        },
        buttons = {
            TextButton(onClick = {
                viewModel.onCancel()
                backHandler.onBackPress()
            }) {
                Text("取消授权")
            }

            Button(
                onClick = {
                    viewModel.onCancel()
                    viewModel.launchInBackground {
                        authByToken(token)
                        withContext(Dispatchers.Main) {
                            backHandler.onBackPress()
                        }
                    }
                },
                enabled = token.isNotEmpty()
            ) {
                Text("用令牌登录")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (Platform.currentPlatform.supportsCallbackLogin) {
                Text("请在打开的窗口中完成登录", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text("登录成功后无响应? 请尝试备用方案", style = MaterialTheme.typography.titleMedium)
            }

            Text("1. 你将前往 Bangumi 开发者测试页面")
            Text("2. 如果提示输入邮箱 (Email), 请使用你的 Bangumi 账号登录")
            Text("3. 创建一个令牌 (token), 名称随意, 有效期随意")
            Text("4. 复制创建好的 token, 回到本页面")

            val context = LocalContext.current
            Button({
                GlobalContext.get().get<BrowserNavigator>()
                    .openBrowser(context, "https://next.bgm.tv/demo/access-token/create")
            }, Modifier.align(Alignment.End)) {
                Text("前往创建令牌")
            }

            Text("在下方粘贴你刚刚复制的 token")

            val clipboard by rememberUpdatedState(LocalClipboardManager.current)
            OutlinedTextField(
                value = token,
                onValueChange = { token = it.trim() },
                label = { Text("Access Token") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    IconButton({
                        clipboard.getText()?.text?.let {
                            token = it.trim()
                        }
                    }) {
                        Icon(Icons.Rounded.ContentPaste, "粘贴")
                    }
                }
            )
        }
    }
}


@Preview
@Composable
private fun PreviewAuthRequestPage() {
    ProvideCompositionLocalsForPreview {
        AuthRequestPage(
            remember { AuthViewModel() },
        )
    }
}
