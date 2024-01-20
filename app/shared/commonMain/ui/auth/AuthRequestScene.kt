package me.him188.ani.app.ui.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.AuthorizationResult
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.TopAppBarActionButton
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.profile.AuthViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun AuthRequestScene(
    vm: AuthViewModel,
    allowBack: Boolean,
    navigator: Navigator,
) {
    val needAuth by vm.needAuth.collectAsStateWithLifecycle()
    if (!needAuth) {
        SideEffect {
            navigator.goBackWith(AuthorizationResult.SUCCESS)
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
                        TopAppBarGoBackButton {
                            navigator.goBackWith(AuthorizationResult.CANCELLED)
                        }
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
