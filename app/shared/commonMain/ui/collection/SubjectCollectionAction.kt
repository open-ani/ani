package me.him188.ani.app.ui.collection

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.openapitools.client.models.SubjectCollectionType


@Stable
val SUBJECT_COLLECTION_ACTIONS = listOf(
    SubjectCollectionAction(
        { Text("想看") },
        { Icon(Icons.Default.ListAlt, null) },
        SubjectCollectionType.Wish
    ),
    SubjectCollectionAction(
        { Text("在看") },
        { Icon(Icons.Default.PlayCircleOutline, null) },
        SubjectCollectionType.Doing
    ),
    SubjectCollectionAction(
        { Text("看过") },
        { Icon(Icons.Default.Done, null) },
        SubjectCollectionType.Done
    ),
    SubjectCollectionAction(
        { Text("搁置") },
        { Icon(Icons.Default.AccessTime, null) },
        SubjectCollectionType.OnHold
    ),
    SubjectCollectionAction(
        { Text("抛弃") },
        { Icon(Icons.Default.Remove, null) },
        SubjectCollectionType.Dropped
    ),
    SubjectCollectionAction(
        { Text("取消追番", color = MaterialTheme.colorScheme.error) },
        { Icon(Icons.Default.DeleteOutline, null) },
        null
    ),
)

@Stable
class SubjectCollectionAction(
    val title: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
    val type: SubjectCollectionType?,
)

@Composable
fun EditCollectionTypeDropDown(
    currentType: SubjectCollectionType,
    showDropdown: Boolean,
    onDismissRequest: () -> Unit,
    onClick: (action: SubjectCollectionAction) -> Unit,
) {
    DropdownMenu(
        showDropdown,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 0.dp, y = 4.dp),
    ) {
        for (action in SUBJECT_COLLECTION_ACTIONS) {
            val onClickState by rememberUpdatedState(onClick)
            val onDismissRequestState by rememberUpdatedState(onDismissRequest)
            val color = if (currentType == action.type) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalContentColor.current
            }
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
