/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

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
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.settings.mediasource.rss.SaveableStorage

@Preview
@Composable
fun PreviewPeerFilterEditItem() {
    var enabled by remember { mutableStateOf(false) }
    var content by remember {
        mutableStateOf(
            """
            filterItem1
            filterItem2
            filterItem3
        """.trimIndent(),
        )
    }

    val item by remember {
        derivedStateOf { PeerFilterItemState(enabled, content) }
    }
    PeerFilterEditItem(
        title = "Peer 过滤器的标题",
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

@Preview
@Composable
fun PreviewPeerFilterEditPane() {
    val config = remember { mutableStateOf(TorrentPeerConfig.Default) }
    val state = remember {
        PeerFilterSettingsState(SaveableStorage(config, { config.value = it }, stateOf(false)))
    }
    PeerFilterEditPane(
        state = state,
        contentPadding = PaddingValues(0.dp),
        showIpBlockingItem = true,
        onClickIpBlockSettings = { },
    )
}