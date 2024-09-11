package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.IconButton
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor

private val IPV4_REGEX = Regex("^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]?[0-9])\$")
private val IPV6_REGEX = Regex("^(?:[\\da-fA-F]{4}:){7}[\\da-fA-F]{4}\$")

private fun validateIp(value: String): Boolean {
    return IPV4_REGEX.matches(value) || IPV6_REGEX.matches(value)
}

@Composable
fun BlockListEditPane(
    blockedIpList: List<String>,
    contentPadding: PaddingValues,
    showTitle: Boolean,
    onAddBlockedIp: (String) -> Unit,
    onRemoveBlockedIp: (String) -> Unit,
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
    
    var showAddBlockedIpDialog by rememberSaveable { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
        if (showTitle) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("黑名单")
                }
                ProvideTextStyleContentColor(MaterialTheme.typography.bodyMedium) {
                    Text("Ani 将总是屏蔽黑名单内的 Peer，无论是否匹配过滤规则")
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
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加黑名单 IP 地址")
                            Text("添加黑名单 IP 地址")
                        }
                    }
                } else {
                    IconButton({
                        showAddBlockedIpDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "添加黑名单 IP 地址")
                    }
                }
                
            },
            colors = listItemColors
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items = blockedIpList, key = { it }) { item ->
                ListItem(
                    headlineContent = { Text(item) },
                    trailingContent = {
                        IconButton({ onRemoveBlockedIp(item) }) {
                            Icon(Icons.Default.Close, contentDescription = "移除此黑名单 IP")
                        }
                    },
                    colors = listItemColors,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    var newBlockedIpValue by rememberSaveable { mutableStateOf("") }
    var isIpValueValid by rememberSaveable { mutableStateOf(true) }
    val dialogAddButtonEnabled by derivedStateOf { newBlockedIpValue.isNotEmpty() }
    
    if (showAddBlockedIpDialog) {
        AlertDialog(
            onDismissRequest = { showAddBlockedIpDialog = false },
            title = { Text("添加 IP 地址") },
            text = {
                Column {
                    Text("向 IP 地址黑名单添加新的 IP 地址")
                    Text("支持 IPv4 或 IPv6 地址，且 IPv6 地址必须为完整格式的地址")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        isError = !isIpValueValid,
                        value = newBlockedIpValue,
                        onValueChange = { 
                            newBlockedIpValue = it
                            isIpValueValid = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isIpValueValid) {
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) { 
                            ProvideContentColor(MaterialTheme.colorScheme.error) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("IP 地址格式错误")
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                Button(
                    enabled = dialogAddButtonEnabled,
                    onClick = { 
                        if (validateIp(newBlockedIpValue)) {
                            showAddBlockedIpDialog = false
                            onAddBlockedIp(newBlockedIpValue)
                            newBlockedIpValue = ""
                        } else {
                            isIpValueValid = false
                        }
                        
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton({
                    showAddBlockedIpDialog = false
                    newBlockedIpValue = ""
                    isIpValueValid = true
                }) {
                    Text("取消")
                }
            }
        )
    }
}