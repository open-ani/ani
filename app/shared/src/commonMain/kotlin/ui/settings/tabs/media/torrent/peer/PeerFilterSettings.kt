package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.platform.navigation.BackHandler
import me.him188.ani.app.ui.foundation.layout.AnimatedPane1
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton

@Composable
fun PeerFilterSettingsPage(
    state: PeerFilterSettingsState,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<Nothing> = rememberListDetailPaneScaffoldNavigator()
) {
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (state.editingIpBlockList) "管理 IP 黑名单" else "Peer 过滤和屏蔽设置" ) },
                navigationIcon = { TopAppBarGoBackButton() }
            )
        }
    ) { paddingValues ->
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane1 { 
                    PeerFilterEditPane(
                        state = state,
                        contentPadding = paddingValues,
                        onClickIpBlockSettings = { navigator.navigateTo(ThreePaneScaffoldRole.Secondary) }
                    )
                }
            },
            detailPane = {
                AnimatedPane1 { 
                    
                }
            }
        )
    }
}