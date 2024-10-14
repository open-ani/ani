package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.richtext.RichText
import me.him188.ani.app.ui.richtext.rememberBBCodeRichTextState

@Composable
private fun BBCodeSupportingText(text: String, modifier: Modifier = Modifier) {
    val richTextState = rememberBBCodeRichTextState(text)
    RichText(richTextState.elements, modifier)
}

@Composable
private fun SwitchItem(
    title: String,
    enabled: Boolean,
    onSwitchChange: (Boolean) -> Unit,
    supportingTextBBCode: String? = null,
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
    ListItem(
        headlineContent = { Text(text = title, overflow = TextOverflow.Ellipsis) },
        supportingContent = supportingTextBBCode?.let {
            { BBCodeSupportingText(supportingTextBBCode) }
        },
        trailingContent = { Switch(checked = enabled, onCheckedChange = onSwitchChange) },
        colors = listItemColors,
        modifier = Modifier.clickable { onSwitchChange(!enabled) },
    )
}

@Composable
private fun ExpandableSwitchItem(
    title: String,
    enabled: Boolean,
    onSwitchChange: (Boolean) -> Unit,
    enabledContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SwitchItem(title, enabled, onSwitchChange)

        AnimatedVisibility(enabled) {
            Column {
                enabledContent()
            }
        }
    }
}

@Composable
private fun RuleEditItem(
    content: String,
    enabled: Boolean,
    supportingTextBBCode: String,
    onContentChange: (String) -> Unit,
    textFieldShape: Shape = MaterialTheme.shapes.extraSmall
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
    ListItem(
        headlineContent = {
            OutlinedTextField(
                value = content,
                enabled = enabled,
                label = { Text("规则") },
                maxLines = 8,
                onValueChange = onContentChange,
                shape = textFieldShape,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        colors = listItemColors,
        supportingContent = {
            BBCodeSupportingText(supportingTextBBCode, Modifier.padding(8.dp))
        }
    )
}

@Composable
fun PeerFilterEditPane(
    state: PeerFilterSettingsState,
    showIpBlockingItem: Boolean,
    onClickIpBlockSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        ListItem(
            headlineContent = {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.labelLarge,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("过滤规则")
                }
            }
        )
        ExpandableSwitchItem(
            title = "过滤 IP 地址",
            enabled = state.ipFilterEnabled,
            onSwitchChange = { state.ipFilterEnabled = it },
            enabledContent = {
                RuleEditItem(
                    content = state.ipFilters,
                    enabled = state.ipFilterEnabled,
                    supportingTextBBCode = """
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
                    onContentChange = { state.ipFilters = it },
                )
            }
        )
        ExpandableSwitchItem(
            title = "过滤客户端指纹",
            enabled = state.idFilterEnabled,
            onSwitchChange = { state.idFilterEnabled = it },
            enabledContent = {
                RuleEditItem(
                    content = state.idFilters,
                    enabled = state.idFilterEnabled,
                    supportingTextBBCode = """
                        每行一条过滤规则，仅支持使用正则表达式过滤
                        例如：[code]\-HP\d{4}\-[/code] 将封禁具有 -HPxxxx- 指纹的客户端
                    """.trimIndent(),
                    onContentChange = { state.idFilters = it },
                )
                SwitchItem(
                    title = "总是过滤异常指纹",
                    enabled = state.blockInvalidId,
                    onSwitchChange = { state.blockInvalidId = it },
                    supportingTextBBCode = """
                        无论是否满足规则, 都会屏蔽指纹不符合 [code]-xxxxxx-[/code] 格式的客户端
                    """.trimIndent()
                )
            }
        )
        ExpandableSwitchItem(
            title = "过滤客户端类型",
            enabled = state.clientFilterEnabled,
            onSwitchChange = { state.clientFilterEnabled = it },
            enabledContent = {
                RuleEditItem(
                    content = state.clientFilters,
                    enabled = state.clientFilterEnabled,
                    supportingTextBBCode = """
                        每行一条过滤规则，仅支持使用正则表达式过滤
                        例如：[code]go\.torrent(\sdev)?[/code] 将封禁百度网盘的离线下载客户端
                    """.trimIndent(),
                    onContentChange = { state.clientFilters = it },
                )
            }
        )

        if (showIpBlockingItem) {
            ListItem(
                headlineContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ProvideTextStyleContentColor(
                            MaterialTheme.typography.labelLarge,
                            MaterialTheme.colorScheme.primary,
                        ) {
                            Text("黑名单")
                        }
                        ProvideTextStyleContentColor(MaterialTheme.typography.labelMedium) {
                            Text("黑名单中的 Peer 总是被屏蔽，无论是否匹配过滤规则")
                        }
                    }
                }
            )
            ListItem(
                headlineContent = { Text(text = "IP 黑名单设置", overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text("点击进入 IP 黑名单列表") },
                trailingContent = {
                    IconButton(onClickIpBlockSettings) {
                        Icon(Icons.Rounded.ArrowOutward, null)
                    }
                },
                colors = listItemColors,
                modifier = Modifier.clickable(onClick = onClickIpBlockSettings),
            )
        }

        ListItem(headlineContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.labelMedium,
                    MaterialTheme.colorScheme.outline,
                ) {
                    Text("提示：修改自动保存")
                }
            }
        })
    }
}