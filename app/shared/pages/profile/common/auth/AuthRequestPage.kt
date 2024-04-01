package me.him188.ani.app.ui.profile.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.feedback.ErrorDialogHost
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.TopAppBarActionButton
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.profile.AuthViewModel
import me.him188.ani.app.ui.profile.BangumiOAuthRequest
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner


@Composable
fun AuthRequestPage(
    vm: AuthViewModel,
    allowBack: Boolean = true,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        vm.onCancel()
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

            val needAuth by vm.needAuth.collectAsStateWithLifecycle()
            if (needAuth) {
                key(vm.retryCount.value) {
                    BangumiOAuthRequest(
                        vm,
                        onFailed = {
                            vm.onAuthFailed(it)
                        },
                        Modifier.fillMaxSize()
                    )
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
        AlertDialog(
            onDismissRequest = {},
            text = { Text("请在打开的窗口中完成登录") },
            confirmButton = {
                TextButton(onClick = { viewModel.onCancel() }) {
                    Text("取消")
                }
            }
        )
    }

    ErrorDialogHost(
        viewModel.authError,
        onConfirm = {
            viewModel.dismissError()
        },
    )
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
