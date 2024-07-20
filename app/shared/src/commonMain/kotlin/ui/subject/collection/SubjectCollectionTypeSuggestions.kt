package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

object SubjectCollectionTypeSuggestions {
    @Composable
    fun Collect(
        state: EditableSubjectCollectionTypeState,
        modifier: Modifier = Modifier,
    ) {
        Box(modifier) {
            SuggestionChip(
                { state.showDropdown = true },
                icon = { Icon(Icons.Rounded.Star, null) },
                label = { Text("追番") },
            )
            EditCollectionTypeDropDown(state)
        }
    }

    @Composable
    fun MarkAsDoing(
        state: EditableSubjectCollectionTypeState,
        modifier: Modifier = Modifier,
    ) {
        Box(modifier) {
            SuggestionChip(
                { state.setSelfCollectionType(UnifiedCollectionType.DOING) },
                icon = SubjectCollectionActions.Doing.icon,
                label = { Text("在看") },
            )
            EditCollectionTypeDropDown(state)
        }
    }

    @Composable
    fun MarkAsDropped(
        state: EditableSubjectCollectionTypeState,
        modifier: Modifier = Modifier,
    ) {
        SuggestionChip(
            { state.setSelfCollectionType(UnifiedCollectionType.DROPPED) },
            icon = SubjectCollectionActions.Dropped.icon,
            label = { Text("抛弃") },
            modifier = modifier,
        )
    }
}
