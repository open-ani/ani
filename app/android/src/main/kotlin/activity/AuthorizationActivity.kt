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
import androidx.compose.ui.Modifier
import me.him188.ani.android.navigation.AndroidAuthorizationNavigator
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.ui.auth.AuthPage
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.TopAppBarActionButton
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.profile.AuthViewModel

class AuthorizationActivity : AniComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allowBack = intent.getBooleanExtra("allowBack", false)

        enableDrawingToSystemBars()

        val vm = AuthViewModel()
        setContent {
            AniApp(currentColorScheme) {
                Scaffold(
                    Modifier
                        .systemBarsPadding()
                        .fillMaxSize(),
                    topBar = {
                        AniTopAppBar(
                            title = { Text(text = "登录 Bangumi") }
                        ) {
                            if (allowBack) {
                                TopAppBarGoBackButton { finish() }
                            }
                            TopAppBarActionButton(onClick = { vm.refresh() }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                ) { contentPadding ->
                    AuthPage(
                        vm,
                        onComplete = {
                            AndroidAuthorizationNavigator.currentResult?.complete(AuthorizationNavigator.AuthorizationResult.SUCCESS)
                            finish()
                        },
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
                AndroidAuthorizationNavigator.currentResult?.complete(AuthorizationNavigator.AuthorizationResult.CANCELLED)
            }
        }
    }

    companion object {
        fun getIntent(context: android.content.Context, allowBack: Boolean): android.content.Intent {
            return android.content.Intent(context, AuthorizationActivity::class.java).apply {
                putExtra("allowBack", allowBack)
            }
        }
    }
}