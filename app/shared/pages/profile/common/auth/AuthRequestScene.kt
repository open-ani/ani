package me.him188.ani.app.ui.profile.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.AuthorizationResult
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

    AuthRequestPage(
        vm,
        allowBack,
        Modifier
    )
}
