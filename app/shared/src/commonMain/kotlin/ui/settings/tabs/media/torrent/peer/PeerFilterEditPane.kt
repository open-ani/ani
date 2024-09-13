package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.components.comment.richtext.RichText
import me.him188.ani.app.ui.subject.components.comment.richtext.rememberBBCodeRichTextState
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor

@Composable
fun PeerFilterEditItem(
    title: String,
    item: PeerFilterItemState,
    onSwitchChange: (Boolean) -> Unit,
    onContentChange: (String) -> Unit,
    editSupportingTextBBCode: String,
    modifier: Modifier = Modifier,
    textFieldShape: Shape = MaterialTheme.shapes.medium
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
    
    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(text = title, overflow = TextOverflow.Ellipsis) },
            trailingContent = { Switch(checked = item.enabled, onCheckedChange = onSwitchChange) },
            colors = listItemColors,
            modifier = Modifier.clickable { onSwitchChange(!item.enabled) }
        )
        
        AnimatedVisibility(item.enabled) {
            val richTextState = rememberBBCodeRichTextState(
                editSupportingTextBBCode, 
                MaterialTheme.typography.bodySmall.fontSize
            )
            
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                OutlinedTextField(
                    value = item.content,
                    enabled = item.enabled,
                    label = { Text("规则") },
                    maxLines = 8,
                    onValueChange = onContentChange,
                    supportingText = { 
                        RichText(
                            richTextState.elements,
                            modifier = Modifier.padding(top = 4.dp)
                        ) 
                    },
                    shape = textFieldShape,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PeerFilterEditPane(
    state: PeerFilterSettingsState,
    contentPadding: PaddingValues,
    showIpBlockingItem: Boolean,
    onClickIpBlockSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

    Column(
        modifier = modifier
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(Modifier.padding(8.dp)) {
            ProvideTextStyleContentColor(
                MaterialTheme.typography.titleMedium,
                MaterialTheme.colorScheme.primary,
            ) {
                Text("过滤规则")
            }
        }
        PeerFilterEditItem(
            title = "过滤 IP 地址",
            editSupportingTextBBCode = """
                    每行一条过滤规则，支持 IPv4 和 IPv6
                    支持以下格式：
                    * 无类别域间路由（CIDR）
                      例如：[code]10.0.0.1/24[/code] 将过滤从 [code]10.0.0.0[/code] 至 [code]10.0.0.255[/code] 的所有 IP
                      [code]ff06:1234::/64[/code] 将过滤从 [code]ff06:1234::[/code] 至 [code]ff06:1234::ffff:ffff:ffff:ffff[/code] 的所有 IP
                    * 通配符
                      例如：[code]10.0.12.*[/code] 将过滤从 [code]10.0.12.0[/code] 至 [code]10.0.12.255[/code] 的所有 IP
                      [code]ff06:1234::*[/code] 将过滤从 [code]ff06:1234::[/code] 至 [code]ff06:1234::ffff[/code] 的所有 IP
                      支持多级通配符，例如 [code]10.0.*.*[/code]
                    * 范围表示
                      例如 [code]10.0.24.100-200[/code] 和 [code]ff06:1234::cafe-dead[/code]
                """.trimIndent(),
            item = PeerFilterItemState(state.ipFilterEnabled, state.ipFilters),
            onSwitchChange = { state.ipFilterEnabled = it },
            onContentChange = { state.ipFilters = it }
        )
        PeerFilterEditItem(
            title = "过滤客户端指纹",
            editSupportingTextBBCode = """
                    每行一条过滤规则，仅支持使用正则表达式过滤
                    例如：[code]\-HP\d{4}\-[/code] 将封禁具有 -HPxxxx- 指纹的客户端
                """.trimIndent(),
            item = PeerFilterItemState(state.idFilterEnabled, state.idFilters),
            onSwitchChange = { state.idFilterEnabled = it },
            onContentChange = { state.idFilters = it }
        )
        PeerFilterEditItem(
            title = "过滤客户端类型",
            editSupportingTextBBCode = """
                    每行一条过滤规则，仅支持使用正则表达式过滤
                    例如：[code]go\.torrent(\sdev)?[/code] 将封禁百度网盘的离线下载客户端
                """.trimIndent(),
            item = PeerFilterItemState(state.clientFilterEnabled, state.clientFilters),
            onSwitchChange = { state.clientFilterEnabled = it },
            onContentChange = { state.clientFilters = it }
        )

        if (showIpBlockingItem) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
            ListItem(
                headlineContent = { Text(text = "IP 黑名单设置", overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text("点击进入 IP 黑名单列表") },
                trailingContent = {
                    IconButton(onClickIpBlockSettings) {
                        Icon(Icons.Rounded.ArrowOutward, null)
                    }
                },
                colors = listItemColors,
                modifier = Modifier.clickable(onClick = onClickIpBlockSettings)
            )
        }

        Row(Modifier.align(Alignment.End).padding(8.dp)) {
            ProvideTextStyleContentColor(
                MaterialTheme.typography.labelMedium,
                MaterialTheme.colorScheme.outline,
            ) {
                Text("提示：修改自动保存")
            }
        }
    }
}