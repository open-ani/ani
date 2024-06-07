package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.feedback.ErrorDialogHost
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.mediaSource.MediaSourceIcon
import me.him188.ani.app.ui.mediaSource.renderMediaSource
import me.him188.ani.app.ui.mediaSource.renderMediaSourceDescription
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.ConnectionTesterResultIndicator
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextItem


@Composable
internal fun SettingsScope.MediaSourceGroup(vm: NetworkSettingsViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    if (showAdd) {
        // 选一个数据源来添加
        BasicAlertDialog(onDismissRequest = { showAdd = false }) {
            SelectMediaSourceTemplateLayout(
                templates = vm.mediaSourceTemplates,
                onClick = {
                    if (it.info.parameters.list.isEmpty()) {
                        // 没有参数, 直接添加
                        vm.confirmEdit(vm.startAdding(it))
                        showAdd = false
                        return@SelectMediaSourceTemplateLayout
                    }
                    vm.startAdding(it)
                },
                onDismissRequest = { showAdd = false }
            )
        }
    }

    ErrorDialogHost(vm.savingError)

    vm.editMediaSourceState?.let {
        // 准备添加这个数据源, 需要配置
        BasicAlertDialog(
            onDismissRequest = { vm.cancelEdit() },
        ) {
            EditMediaSourceLayout(
                it,
                onConfirm = {
                    vm.confirmEdit(it)
                    showAdd = false
                },
                onDismissRequest = {
                    vm.cancelEdit()
                    showAdd = false
                }
            )
        }
    }

    Group(
        title = { Text("数据源管理") },
        description = { Text("点击编辑，长按调整使用顺序") },
        actions = {
            IconButton({
                vm.cancelEdit()
                showAdd = true
            }) {
                Icon(Icons.Rounded.Add, contentDescription = "添加数据源")
            }
        }
    ) {
        vm.mediaSources.forEachIndexed { index, item ->
            if (index != 0) {
                HorizontalDividerItem()
            }
            MediaSourceItem(
                item,
                onClick = { vm.startEditing(item) }
            )
        }

        HorizontalDividerItem()


        TextButtonItem(
            onClick = {
                vm.mediaSourceTesters.toggleTest()
            },
            title = {
                if (vm.mediaSourceTesters.anyTesting) {
                    Text("终止测试")
                } else {
                    Text("开始测试")
                }
            },
        )
    }
}


@Composable
internal fun SettingsScope.MediaSourceItem(
    item: MediaSourcePresentation,
    onClick: () -> Unit,
    title: @Composable RowScope.() -> Unit = { Text(remember(item.mediaSourceId) { renderMediaSource(item.mediaSourceId) }) },
    description: (@Composable () -> Unit)? =
        renderMediaSourceDescription(item.mediaSourceId)?.let {
            { Text(it) }
        },
    icon: (@Composable () -> Unit)? = {
        Box(Modifier.clip(MaterialTheme.shapes.extraSmall).size(48.dp), contentAlignment = Alignment.Center) {
            MediaSourceIcon(item.mediaSourceId)
        }
    },
) {
    TextItem(
        title = title,
        icon = icon,
        description = description,
        action = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConnectionTesterResultIndicator(item.connectionTester)


            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
internal fun SelectMediaSourceTemplateLayout(
    templates: List<MediaSourceTemplate>,
    onClick: (MediaSourceTemplate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onClickState by rememberUpdatedState(onClick)
    RichDialogLayout(
        title = { Text("添加数据源") },
        description = { Text("选择模板") },
        buttons = {
            TextButton(onDismissRequest) {
                Text("取消")
            }
        },
        modifier = modifier,
    ) {
        SettingsTab(Modifier.heightIn(max = 400.dp)) {
            for (item in templates) {
                ElevatedCard {
                    TextItem(
                        title = { Text(remember(item.mediaSourceId) { renderMediaSource(item.mediaSourceId) }) },
                        description = renderMediaSourceDescription(item.mediaSourceId)?.let {
                            { Text(it) }
                        },
                        onClick = { onClickState(item) },
                        icon = {
                            Box(Modifier.clip(MaterialTheme.shapes.extraSmall).size(48.dp)) {
                                MediaSourceIcon(item.mediaSourceId)
                            }
                        },
                    )
                }
            }
        }
    }
}
