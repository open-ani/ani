package me.him188.ani.app.ui.profile.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.him188.ani.app.ViewModelAuthSupport
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.session.BangumiAuthorizationConstants
import me.him188.ani.app.session.OAuthResult
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.utils.logging.error
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/*
 * 注意: 此页面其实没有使用到, 只是留作备份
 */


private class AuthResultViewModel : AbstractViewModel(), KoinComponent, ViewModelAuthSupport {
    private val sessionManager: SessionManager by inject()

    fun submitCode(code: String) {
        try {
            sessionManager.processingRequest.value?.onCallback(
                Result.success(
                    OAuthResult(
                        code,
                        BangumiAuthorizationConstants.CALLBACK_URL
                    )
                )
            )
        } catch (e: Throwable) {
            logger.error(e) { "AuthResultViewModel: Failed to submit code" }
        }
    }
}

@Composable
fun AuthResultPage(
    code: String,
    modifier: Modifier = Modifier
) {
    val vm = remember { AuthResultViewModel() }

    val navigator = LocalNavigator.current

    SideEffect {
        vm.submitCode(code)
        navigator.navigator.goBack()
    }

    Scaffold(
        modifier
            .systemBarsPadding()
            .fillMaxSize(),
        topBar = {
            AniTopAppBar(
                actions = {
                    TopAppBarGoBackButton {
                        navigator.navigator.goBack()
                    }
                },
                title = { Text(text = "登录 Bangumi") }
            )
        }
    ) { contentPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "正在处理...")
            }
        }
    }
}


@Composable
fun AuthResultScene(
    code: String,
    modifier: Modifier = Modifier,
) {
    AuthResultPage(code, modifier)
}

@Preview
@Composable
private fun PreviewAuthResultScene() {
    AuthResultScene("123456")
}