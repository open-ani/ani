package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.feedback.ErrorDialogHost
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.settings.framework.ConnectionTesterResultIndicator
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextButtonItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.app.ui.settings.rendering.MediaSourceIcon
import me.him188.ani.app.ui.settings.rendering.renderMediaSource
import me.him188.ani.app.ui.settings.rendering.renderMediaSourceDescription
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable

@Composable
internal fun SettingsScope.MediaSourceGroup(vm: NetworkSettingsViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    if (showAdd) {
        // 选一个数据源来添加
        SelectMediaSourceTemplateDialog(
            templates = vm.availableMediaSourceTemplates,
            onClick = {
                if (it.info.parameters.list.isEmpty()) {
                    // 没有参数, 直接添加
                    vm.confirmEdit(vm.startAdding(it))
                    showAdd = false
                    return@SelectMediaSourceTemplateDialog
                }
                vm.startAdding(it)
            },
            onDismissRequest = { showAdd = false },
        )
    }

    ErrorDialogHost(vm.savingError)

    vm.editMediaSourceState?.let {
        // 准备添加这个数据源, 需要配置
        BasicAlertDialog(
            onDismissRequest = { vm.cancelEdit() },
        ) {
            EditMediaSourceDialog(
                it,
                onConfirm = {
                    vm.confirmEdit(it)
                    showAdd = false
                },
                onDismissRequest = {
                    vm.cancelEdit()
                    showAdd = false
                },
            )
        }
    }

    val sorter = rememberSorterState<MediaSourcePresentation>(
        onComplete = { list -> vm.reorderMediaSources(newOrder = list.map { it.instanceId }) },
    )
    Group(
        title = { Text("数据源管理") },
        actions = {
            AnimatedVisibility(
                visible = sorter.isSorting,
            ) {
                Row {
                    IconButton({ sorter.cancel() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "取消排序")
                    }
                }
            }
            AnimatedVisibility(
                visible = !sorter.isSorting,
            ) {
                Row {
                    IconButton(
                        {
                            vm.cancelEdit()
                            showAdd = true
                        },
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "添加数据源")
                    }
                }
            }
            Crossfade(sorter.isSorting, Modifier.animateContentSize()) { isSorting ->
                if (isSorting) {
                    Button(
                        {
                            sorter.complete()
                        },
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = "保存排序")
                    }
                } else {
                    IconButton(
                        {
                            vm.cancelEdit()
                            sorter.start(vm.mediaSources)
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "排序")
                    }
                }
            }
        },
    ) {
        Box {
            Column(
                Modifier
                    .ifThen(sorter.isSorting) { alpha(0f) }
                    .wrapContentHeight()
                    .placeholder(vm.isCompletingReorder),
            ) {
                vm.mediaSources.forEachIndexed { index, item ->
                    if (index != 0) {
                        HorizontalDividerItem()
                    }
                    MediaSourceItem(
                        item,
                        Modifier.combinedClickable(
                            onClickLabel = "编辑",
                            onLongClick = { sorter.start(vm.mediaSources) },
                            onLongClickLabel = "开始排序",
                        ) {
                            vm.startEditing(
                                item,
                            )
                        },
                    ) {
                        NormalMediaSourceItemAction(
                            item,
                            onEdit = { vm.startEditing(item) },
                            onDelete = { vm.deleteMediaSource(item) },
                            onEnabledChange = { vm.toggleMediaSourceEnabled(item, it) },
                        )
                    }
                }
            }
            if (sorter.isSorting) {
                // 往上面再盖一层, 因为 SettingsTab 已经有 scrollable 了, LazyColumn 如果不加高度限制会出错
                LazyColumn(
                    state = sorter.listState,
                    modifier = Modifier
                        .matchParentSize()
                        .reorderable(sorter.reorderableState)
                        .detectReorderAfterLongPress(sorter.reorderableState),
                ) {
                    itemsIndexed(
                        sorter.sortingData,
                        key = { _, item -> item.instanceId },
                    ) { index, item ->
                        if (index != 0) {
                            HorizontalDividerItem()
                        }
                        ReorderableItem(sorter.reorderableState, key = item.instanceId) { isDragging ->
                            val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                            MediaSourceItem(
                                item,
                                Modifier
                                    .shadow(elevation.value)
                                    .background(MaterialTheme.colorScheme.surface), // match card background
                            ) {
                                Icon(
                                    Icons.Rounded.Reorder,
                                    "拖拽排序",
                                    Modifier.detectReorder(sorter.reorderableState),
                                )
                            }
                        }
                    }
                }
            } else {
                // 清空 list 状态, 否则在删除一个项目后再切换到排序状态, 有的项目会消失
                LazyColumn(Modifier.height(0.dp), sorter.listState) { }
            }
        }

        HorizontalDividerItem()


        TextButtonItem(
            onClick = {
                vm.mediaSourceTesters.toggleTest()
            },
            Modifier.ifThen(sorter.isSorting) { alpha(0f) },
            enabled = !sorter.isSorting,
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


private const val DISABLED_ALPHA = 0.38f

@Composable
internal fun SettingsScope.MediaSourceItem(
    item: MediaSourcePresentation,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = item.isEnabled,
    title: @Composable RowScope.() -> Unit = {
        Text(
            item.info.name,
            Modifier.ifThen(!isEnabled) { alpha(DISABLED_ALPHA) },
        )
    },
    description: (@Composable () -> Unit)? =
        item.info.description?.let {
            {
                Text(it, Modifier.ifThen(!isEnabled) { alpha(DISABLED_ALPHA) })
            }
        },
    icon: (@Composable () -> Unit)? = {
        Box(
            Modifier.ifThen(!isEnabled) { alpha(DISABLED_ALPHA) }.clip(MaterialTheme.shapes.extraSmall).size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            MediaSourceIcon(item.mediaSourceId, Modifier.size(48.dp), item.info.iconUrl)
        }
    },
    action: @Composable () -> Unit,
) {
    TextItem(
        modifier = modifier,
        description = description,
        icon = icon,
        action = action,
        title = title,
    )
}

@Composable
internal fun NormalMediaSourceItemAction(
    item: MediaSourcePresentation,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEnabledChange: (enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton({}, enabled = false) { // 放在 button 里保持 padding 一致
            ConnectionTesterResultIndicator(
                item.connectionTester,
                showIdle = false,
            )
        }

        var showConfirmDelete by remember { mutableStateOf(false) }
        if (showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                icon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("删除数据源") },
                text = { Text("同时会删除该数据源的配置，确认删除吗？") },
                confirmButton = {
                    TextButton({ onDelete(); showConfirmDelete = false }) {
                        Text(
                            "删除",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = { TextButton({ showConfirmDelete = false }) { Text("取消") } },
            )
        }

        Box {
            var showMore by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = showMore,
                onDismissRequest = { showMore = false },
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        if (item.isEnabled) {
                            Icon(Icons.Rounded.VisibilityOff, null)
                        } else {
                            Icon(Icons.Rounded.Visibility, null)
                        }
                    },
                    text = {
                        if (item.isEnabled) {
                            Text("禁用")
                        } else {
                            Text("启用")
                        }
                    },
                    onClick = {
                        onEnabledChange(!item.isEnabled)
                        showMore = false
                    },
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                    text = { Text("编辑") }, // 直接点击数据源一行也可以编辑, 但还是在这里放一个按钮以免有人不知道
                    onClick = {
                        showMore = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMore = false
                        showConfirmDelete = true
                    },
                )
            }

            IconButton({ showMore = true }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "更多",
                )
            }
        }
    }
}

@Composable
internal fun SelectMediaSourceTemplateDialog(
    templates: List<MediaSourceTemplate>,
    onClick: (MediaSourceTemplate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("选择模板")
        },
        confirmButton = {
            TextButton(onDismissRequest) {
                Text("取消")
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(templates) { item ->
                    MediaSourceCard(
                        onClick = { onClick(item) },
                        title = {
                            Text(
                                remember(item.mediaSourceId) { renderMediaSource(item.mediaSourceId) },
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        },
                        icon = {
                            Box(Modifier.clip(MaterialTheme.shapes.extraSmall).size(48.dp)) {
                                MediaSourceIcon(item.mediaSourceId, Modifier.size(48.dp), item.info.iconUrl)
                            }
                        },
                        content = {
                            renderMediaSourceDescription(item.mediaSourceId)?.let {
                                Text(it)
                            }
                        },
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun MediaSourceCard(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(onClick, modifier) {
        Column(
            Modifier.align(Alignment.CenterHorizontally).padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.wrapContentSize().align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                icon?.let {
                    Box(Modifier.wrapContentSize().size(24.dp), contentAlignment = Alignment.Center) {
                        it()
                    }
                }
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    title()
                }
            }

            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                content()
            }
        }
    }
}