package me.him188.ani.android.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.android.navigation.AndroidAuthorizationNavigator
import me.him188.ani.app.navigation.AuthorizationNavigator.AuthorizationResult
import me.him188.ani.app.ui.auth.AuthRequestPage
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.TopAppBarActionButton
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.profile.AuthViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


class AuthorizationActivity : AniComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allowBack = intent.getBooleanExtra("allowBack", false)

        enableDrawingToSystemBars()

        setContent {
            val vm = remember { AuthViewModel().also { currentAuthViewModel = it } }

            AniApp(currentColorScheme) {
                val needAuth by vm.needAuth.collectAsStateWithLifecycle()
                if (!needAuth) {
                    SideEffect {
                        AndroidAuthorizationNavigator.currentResult?.complete(AuthorizationResult.SUCCESS)
                        finish()
                    }
                }

                Scaffold(
                    Modifier
                        .systemBarsPadding()
                        .fillMaxSize(),
                    topBar = {
                        AniTopAppBar(
                            actions = {
                                if (allowBack) {
                                    TopAppBarGoBackButton { finish() }
                                }
                                TopAppBarActionButton(onClick = { vm.refresh() }) {
                                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                                }
                            },
                            title = { Text(text = "登录 Bangumi") }
                        )
                    }
                ) { contentPadding ->
                    AuthRequestPage(
                        vm,
                        Modifier.padding(contentPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidAuthorizationNavigator.currentResult?.let {
            if (it.isActive) {
                it.complete(AuthorizationResult.CANCELLED)
            }
        }
    }

    companion object {
        var currentAuthViewModel: AuthViewModel? = AuthViewModel()

        fun getIntent(context: android.content.Context, allowBack: Boolean): android.content.Intent {
            return android.content.Intent(context, AuthorizationActivity::class.java).apply {
                putExtra("allowBack", allowBack)
            }
        }
    }
}