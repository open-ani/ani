package me.him188.ani.app.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.him188.ani.app.ViewModelAuthSupport
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.requireOnline
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.rememberViewModel
import org.koin.core.component.KoinComponent

private class UnauthorizedTipsViewModel : AbstractViewModel(), KoinComponent, ViewModelAuthSupport

@Composable
fun UnauthorizedTips(
    modifier: Modifier = Modifier,
) {
    val vm = rememberViewModel { UnauthorizedTipsViewModel() }

    val navigator = LocalNavigator.current
    Box(modifier, contentAlignment = Alignment.Center) {
        TextButton({ vm.requireOnline(navigator = navigator) }) {
            Text("请先登录", style = MaterialTheme.typography.titleMedium)
        }
    }
}