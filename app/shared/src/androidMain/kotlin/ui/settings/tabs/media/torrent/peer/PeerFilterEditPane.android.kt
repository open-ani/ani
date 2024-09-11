package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf

@Preview
@Composable
fun PreviewPeerFilterEditItem() {
    ProvideCompositionLocalsForPreview {
        var enabled by remember { mutableStateOf(false) }
        var content by remember { mutableStateOf("""
            filterItem1
            filterItem2
            filterItem3
        """.trimIndent()) }
        
        val item by remember {
            derivedStateOf { PeerFilterItemState(enabled, content) }
        }
        PeerFilterEditItem(
            title = "Peer 过滤器的标题",
            description = "点击启用过滤器",
            item = item,
            editSupportingTextBBCode = """
                Peer 过滤器的说明
                这是第二行的说明
                这是第三行说明
            """,
            onSwitchChange = { enabled = it },
            onContentChange = { content = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
fun PreviewPeerFilterEditPane() {
    ProvideCompositionLocalsForPreview {
        val config = remember { mutableStateOf(TorrentPeerConfig.Default) }
        val state = remember { 
            PeerFilterSettingsState(config, { config.value = it }, stateOf(false)) 
        }
        PeerFilterEditPane(
            state = state, 
            contentPadding = PaddingValues(0.dp), 
            showIpBlockingItem = true,
            onClickIpBlockSettings = { }
        )
    }
}