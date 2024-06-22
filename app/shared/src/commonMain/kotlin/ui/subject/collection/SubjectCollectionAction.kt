package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Immutable
object SubjectCollectionActions {
    val Wish = SubjectCollectionAction(
        { Text("想看") },
        { Icon(Icons.AutoMirrored.Rounded.ListAlt, null) },
        UnifiedCollectionType.WISH,
    )
    val Doing = SubjectCollectionAction(
        { Text("在看") },
        { Icon(Icons.Rounded.PlayArrow, null) },
        UnifiedCollectionType.DOING,
    )
    val Done = SubjectCollectionAction(
        { Text("看过") },
        { Icon(Icons.Rounded.Done, null) },
        UnifiedCollectionType.DONE,
    )
    val OnHold = SubjectCollectionAction(
        { Text("搁置") },
        { Icon(Icons.Rounded.AccessTime, null) },
        UnifiedCollectionType.ON_HOLD,
    )
    val Dropped = SubjectCollectionAction(
        { Text("抛弃") },
        { Icon(Icons.Rounded.Remove, null) },
        UnifiedCollectionType.DROPPED,
    )
    val DeleteCollection = SubjectCollectionAction(
        { Text("取消追番", color = MaterialTheme.colorScheme.error) },
        { Icon(Icons.Rounded.DeleteOutline, null) },
        type = UnifiedCollectionType.NOT_COLLECTED,
    )
    val Collect = SubjectCollectionAction(
        { Text("追番") },
        { Icon(Icons.Rounded.Star, null) },
        type = UnifiedCollectionType.NOT_COLLECTED,
    )
}

private val SubjectCollectionActionsCommon
    get() = listOf(
        SubjectCollectionActions.Wish,
        SubjectCollectionActions.Doing,
        SubjectCollectionActions.Done,
        SubjectCollectionActions.OnHold,
        SubjectCollectionActions.Dropped,
    )

@Stable
val SubjectCollectionActionsForEdit = SubjectCollectionActionsCommon + listOf(
    SubjectCollectionActions.DeleteCollection,
)

@Stable
val SubjectCollectionActionsForCollect = SubjectCollectionActionsCommon + listOf(
    SubjectCollectionActions.Collect,
)

@Immutable
class SubjectCollectionAction(
    val title: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
    val type: UnifiedCollectionType,
)

/**
 * A drop down menu to edit the collection type of a subject.
 * Also includes a dialog to set all episodes as watched when the user attempts to mark the subject as [UnifiedCollectionType.DONE].
 */
@Composable
fun EditCollectionTypeDropDown(
    currentType: UnifiedCollectionType?,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSetAllEpisodesDone: (() -> Unit)?,
    onClick: (action: SubjectCollectionAction) -> Unit,
    actions: List<SubjectCollectionAction> = SubjectCollectionActionsForEdit,
) {
    // 同时设置所有剧集为看过
    var showSetAllEpisodesDialog by rememberSaveable { mutableStateOf(false) }
    if (showSetAllEpisodesDialog && onSetAllEpisodesDone != null) {
        AlertDialog(
            onDismissRequest = {
                showSetAllEpisodesDialog = false
            },
            text = {
                Text("要同时设置所有剧集为看过吗？")
            },
            confirmButton = {
                Button(
                    {
                        showSetAllEpisodesDialog = false
                        onSetAllEpisodesDone.invoke()
                    },
                ) {
                    Text("设置")
                }
            },
            dismissButton = {
                TextButton(
                    {
                        showSetAllEpisodesDialog = false
                    },
                ) {
                    Text("取消")
                }
            },
        )
    }

    DropdownMenu(
        expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 0.dp, y = 4.dp),
    ) {
        for (action in actions) {
            val onClickState by rememberUpdatedState(onClick)
            val onDismissRequestState by rememberUpdatedState(onDismissRequest)
            val color = action.colorForCurrent(currentType)
            DropdownMenuItem(
                text = {
                    CompositionLocalProvider(LocalContentColor provides color) {
                        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                            action.title()
                        }
                    }
                },
                leadingIcon = {
                    CompositionLocalProvider(LocalContentColor provides color) {
                        action.icon()
                    }
                },
                onClick = {
                    onClickState(action)
                    onDismissRequestState()
                    if (action == SubjectCollectionActions.Done) {
                        showSetAllEpisodesDialog = true
                    }
                },
            )
        }
    }
}

@Composable
private fun SubjectCollectionAction.colorForCurrent(
    currentType: UnifiedCollectionType?
) = if (currentType == type) {
    MaterialTheme.colorScheme.primary
} else {
    LocalContentColor.current
}


private val CollectedActionButtonColors
    @Composable
    get() =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.outlineVariant,
            contentColor = MaterialTheme.colorScheme.outline,
        )

private val UncollectedActionButtonColors
    @Composable
    get() =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )


/**
 * 展示当前收藏类型的按钮, 点击时可以弹出菜单选择要修改的收藏类型.
 *
 * @param collected 是否已收藏, 为 `null` 时表示正在载入.
 * @param type 当前收藏类型的相应动作, 例如未追番时为 "追番", 已追番时为 "已在看" / "已看完" 等. 为 `null` 时表示正在载入.
 * @param onCollect 当收藏时调用.
 * @param onEdit 当修改类型时调用.
 */
@Composable
fun CollectionActionButton(
    collected: Boolean?,
    type: UnifiedCollectionType?,
    onCollect: () -> Unit,
    onEdit: (newType: UnifiedCollectionType) -> Unit,
    onSetAllEpisodesDone: () -> Unit,
) {
    val action = remember(type) {
        SubjectCollectionActionsForCollect.find { it.type == type }
    }
    val collectedState by rememberUpdatedState(collected)
    val onCollectState by rememberUpdatedState(onCollect)
    Box(Modifier.placeholder(collected == null || type == null)) {
        var showDropdown by rememberSaveable { mutableStateOf(false) }
        val onClick = remember {
            {
                when (collectedState) {
                    null -> {}
                    false -> onCollectState()
                    true -> showDropdown = true
                }
            }
        }
        if (collected == true) {
            FilledTonalButton(
                onClick = onClick,
            ) {
                if (action != null) {
                    action.icon()
                    Row(Modifier.padding(start = 8.dp)) {
                        action.title()
                    }
                } else {
                    Text("载入") // 随便什么都行, 占空间
                }
            }
        } else {
            Button(
                onClick = onClick,
            ) {
                if (action != null) {
                    action.icon()
                    Row(Modifier.padding(start = 8.dp)) {
                        action.title()
                    }
                } else {
                    Text("载入") // 随便什么都行, 占空间
                }
            }

        }
        EditCollectionTypeDropDown(
            currentType = type,
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            onSetAllEpisodesDone = onSetAllEpisodesDone,
            onClick = {
                showDropdown = false
                onEdit(it.type)
            },
        )
    }
}

@Composable
private fun BasicSubjectCollectionActionButton(
    action: SubjectCollectionAction?,
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier,
        colors = colors,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (action != null) {
                action.icon()
                Row(Modifier.padding(start = 8.dp)) {
                    action.title()
                }
            } else {
                Text("载入") // 随便什么都行, 占空间
            }
        }

    }

}
