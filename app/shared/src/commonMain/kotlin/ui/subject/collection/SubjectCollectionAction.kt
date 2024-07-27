package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Immutable
object SubjectCollectionActions {
    val Wish = SubjectCollectionAction(
        { Text("想看") },
        { Icon(Icons.AutoMirrored.Rounded.EventNote, null) },
        UnifiedCollectionType.WISH,
    )
    val Doing = SubjectCollectionAction(
        { Text("在看") },
        { Icon(Icons.Rounded.PlayCircleOutline, null) },
        UnifiedCollectionType.DOING,
    )
    val Done = SubjectCollectionAction(
        { Text("看过") },
        { Icon(Icons.Rounded.TaskAlt, null) },
        UnifiedCollectionType.DONE,
    )
    val OnHold = SubjectCollectionAction(
        { Text("搁置") },
        { Icon(Icons.Rounded.AccessTime, null) },
        UnifiedCollectionType.ON_HOLD,
    )
    val Dropped = SubjectCollectionAction(
        { Text("抛弃") },
        { Icon(Icons.Rounded.Block, null) },
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


@Composable
fun EditCollectionTypeDropDown(
    state: EditableSubjectCollectionTypeState,
    modifier: Modifier = Modifier,
) {
    EditCollectionTypeDropDown(
        currentType = state.selfCollectionType,
        expanded = state.showDropdown,
        onDismissRequest = { state.showDropdown = false },
        onClick = {
            state.showDropdown = false
            state.setSelfCollectionType(it.type)
        },
        modifier = modifier,
    )
}

/**
 * A drop down menu to edit the collection type of a subject.
 * Also includes a dialog to set all episodes as watched when the user attempts to mark the subject as [UnifiedCollectionType.DONE].
 */
@Composable
fun EditCollectionTypeDropDown(
    currentType: UnifiedCollectionType?,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (action: SubjectCollectionAction) -> Unit,
    modifier: Modifier = Modifier,
    actions: List<SubjectCollectionAction> = SubjectCollectionActionsForEdit,
    showDelete: Boolean = currentType != UnifiedCollectionType.NOT_COLLECTED,
) {
    DropdownMenu(
        expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 0.dp, y = 4.dp),
        modifier = modifier,
    ) {
        for (action in actions) {
            if (!showDelete && action == SubjectCollectionActions.DeleteCollection) continue

            val onClickState by rememberUpdatedState(onClick)
            val onDismissRequestState by rememberUpdatedState(onDismissRequest)
            val color = action.colorForCurrent(currentType)
            DropdownMenuItem(
                text = {
                    CompositionLocalProvider(LocalContentColor provides color) {
                        action.title()
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

object SubjectCollectionTypeButtonDefaults {
    @Composable
    fun collectedButtonColors() = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    fun collectedBorder() = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.612f))
}

/**
 * 展示当前收藏类型的按钮, 点击时可以弹出菜单选择要修改的收藏类型.
 *
 * @param collected 是否已收藏, 为 `null` 时表示正在载入.
 * @param type 当前收藏类型的相应动作, 例如未追番时为 "追番", 已追番时为 "已在看" / "已看完" 等. 为 `null` 时表示正在载入.
 * @param onEdit 当修改类型时调用.
 */
@Composable
fun SubjectCollectionTypeButton(
    type: UnifiedCollectionType,
    onEdit: (newType: UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    collected: Boolean = type != UnifiedCollectionType.NOT_COLLECTED,
    enabled: Boolean = true,
    collectedColors: ButtonColors = SubjectCollectionTypeButtonDefaults.collectedButtonColors(),
    collectedBorder: BorderStroke = SubjectCollectionTypeButtonDefaults.collectedBorder(),
) {
    val action = remember(type) {
        SubjectCollectionActionsForCollect.find { it.type == type }
    }
    Box(modifier) {
        var showDropdown by rememberSaveable { mutableStateOf(false) }
        val onClick = remember {
            {
                showDropdown = true
            }
        }
        if (collected) {
            OutlinedButton(
                onClick = onClick,
                colors = collectedColors,
                border = collectedBorder,
                enabled = enabled,
            ) {
                if (action != null) {
                    action.icon()
                    Row(Modifier.padding(start = 8.dp)) {
                        Text(renderCollectionTypeAsCurrent(type))
                    }
                } else {
                    Text("载入") // 随便什么都行, 占空间
                }
            }
        } else {
            Button(
                onClick = onClick,
                enabled = enabled,
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
            onClick = {
                showDropdown = false
                onEdit(it.type)
            },
        )
    }
}

@Stable
private fun renderCollectionTypeAsCurrent(type: UnifiedCollectionType): String {
    return when (type) {
        UnifiedCollectionType.WISH -> "已想看"
        UnifiedCollectionType.DOING -> "已在看"
        UnifiedCollectionType.DONE -> "已看过"
        UnifiedCollectionType.ON_HOLD -> "已搁置"
        UnifiedCollectionType.DROPPED -> "已抛弃"
        UnifiedCollectionType.NOT_COLLECTED -> "未追番"
    }
}

@Composable
fun SetAllEpisodeDoneDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Rounded.TaskAlt, null) },
        text = { Text("要同时设置所有剧集为看过吗？") },
        confirmButton = { TextButton(onConfirm) { Text("设置") } },
        dismissButton = { TextButton(onDismissRequest) { Text("忽略") } },
        modifier = modifier,
    )
}
