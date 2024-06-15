package me.him188.ani.app.ui.profile.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.LocalBackHandler
import me.him188.ani.app.ui.profile.AuthViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun AuthRequestScene(
    vm: AuthViewModel,
    allowBack: Boolean,
) {
    val needAuth by vm.needAuth.collectAsStateWithLifecycle()
    if (!needAuth) {
        val backHandler = LocalBackHandler.current
        SideEffect {
            backHandler.onBackPress()
        }
    }

    AuthRequestPage(
        vm,
        allowBack,
        Modifier
    )
}
