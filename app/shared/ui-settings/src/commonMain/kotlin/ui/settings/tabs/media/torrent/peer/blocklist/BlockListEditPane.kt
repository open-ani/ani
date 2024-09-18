/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.media.torrent.peer.blocklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.IconButton
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor


@Composable
fun BlockListEditPane(
    blockedIpList: List<String>,
    contentPadding: PaddingValues,
    showTitle: Boolean,
    onAdd: (List<String>) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

    var showAddBlockedIpDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.padding(contentPadding).fillMaxSize()) {
        if (showTitle) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("黑名单")
                }
                ProvideTextStyleContentColor(MaterialTheme.typography.bodyMedium) {
                    Text("黑名单中的 Peer 总是被屏蔽，无论是否匹配过滤规则")
                }
            }
        }
        ListItem(
            headlineContent = {
                if (!showTitle) Text("添加黑名单 IP 地址")
            },
            trailingContent = {
                if (showTitle) {
                    OutlinedButton({ showAddBlockedIpDialog = true }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加黑名单 IP 地址")
                            Text("添加黑名单 IP 地址")
                        }
                    }
                } else {
                    IconButton(
                        {
                            showAddBlockedIpDialog = true
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加黑名单 IP 地址")
                    }
                }

            },
            colors = listItemColors,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = blockedIpList, key = { it }) { item ->
                ListItem(
                    headlineContent = { Text(item) },
                    trailingContent = {
                        IconButton({ onRemove(item) }) {
                            Icon(Icons.Default.Close, contentDescription = "移除此黑名单 IP")
                        }
                    },
                    colors = listItemColors,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (showAddBlockedIpDialog) {
        AddBlockedIPDialog(
            onAdd = onAdd,
            onDismiss = { showAddBlockedIpDialog = false }
        )
    }
}