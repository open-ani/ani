package me.him188.ani.app.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.datasources.api.CollectionType

@Immutable
object SubjectCollectionActions {
    val Wish = SubjectCollectionAction(
        { Text("想看") },
        { Icon(Icons.Default.ListAlt, null) },
        CollectionType.Wish
    )
    val Doing = SubjectCollectionAction(
        { Text("在看") },
        { Icon(Icons.Default.PlayCircleOutline, null) },
        CollectionType.Doing
    )
    val Done = SubjectCollectionAction(
        { Text("看过") },
        { Icon(Icons.Default.Done, null) },
        CollectionType.Done
    )
    val OnHold = SubjectCollectionAction(
        { Text("搁置") },
        { Icon(Icons.Default.AccessTime, null) },
        CollectionType.OnHold
    )
    val Dropped = SubjectCollectionAction(
        { Text("抛弃") },
        { Icon(Icons.Default.Remove, null) },
        CollectionType.Dropped
    )
    val DeleteCollection = SubjectCollectionAction(
        { Text("取消追番", color = MaterialTheme.colorScheme.error) },
        { Icon(Icons.Default.DeleteOutline, null) },
        type = CollectionType.NotCollected,
    )
    val Collect = SubjectCollectionAction(
        { Text("追番") },
        { Icon(Icons.Default.Star, null) },
        type = CollectionType.NotCollected,
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
    val type: CollectionType,
)

@Composable
fun EditCollectionTypeDropDown(
    currentType: CollectionType?,
    showDropdown: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (action: SubjectCollectionAction) -> Unit,
    actions: List<SubjectCollectionAction> = SubjectCollectionActionsForEdit
) {
    DropdownMenu(
        showDropdown,
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
                }
            )
        }
    }
}

@Composable
private fun SubjectCollectionAction.colorForCurrent(
    currentType: CollectionType?
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
            contentColor = MaterialTheme.colorScheme.outline
        )

private val UncollectedActionButtonColors
    @Composable
    get() =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
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
    type: CollectionType?,
    onCollect: () -> Unit,
    onEdit: (newType: CollectionType) -> Unit,
) {
    val action = remember(type) {
        SubjectCollectionActionsForCollect.find { it.type == type }
    }
    Box(Modifier.placeholder(collected == null || type == null)) {
        var showDropdown by remember { mutableStateOf(false) }
        BasicSubjectCollectionActionButton(
            action,
            onClick = {
                when (collected) {
                    null -> return@BasicSubjectCollectionActionButton
                    false -> onCollect()
                    true -> showDropdown = true
                }
            },
            colors = if (collected == true) CollectedActionButtonColors else UncollectedActionButtonColors
        )

        EditCollectionTypeDropDown(
            currentType = type,
            showDropdown = showDropdown,
            onDismissRequest = { showDropdown = false },
            onClick = {
                showDropdown = false
                onEdit(it.type)
            },
        )
    }
}

@Composable
fun BasicSubjectCollectionActionButton(
    action: SubjectCollectionAction?,
    onClick: () -> Unit,
    colors: ButtonColors,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier,
        colors = colors,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (action != null) {
                Box(Modifier.size(16.dp)) {
                    action.icon()
                }

                Row(Modifier.padding(start = 8.dp)) {
                    action.title()
                }
            } else {
                Text("载入") // 随便什么都行, 占空间
            }
        }

    }

}
