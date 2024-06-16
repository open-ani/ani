package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.subject.collection.EditCollectionTypeDropDown
import me.him188.ani.app.ui.subject.collection.SubjectCollectionAction
import me.him188.ani.app.ui.subject.collection.SubjectCollectionActions
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


private val ACTIONS = listOf(
    SubjectCollectionAction(
        { Text("取消看过") },
        { Icon(Icons.Rounded.AccessTime, null) },
        UnifiedCollectionType.WISH
    ),
    SubjectCollectionActions.Done,
    SubjectCollectionActions.Dropped,
)

@Composable
fun EpisodeCollectionActionButton(
    collectionType: UnifiedCollectionType?,
    onClick: (target: UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var showDropdown by rememberSaveable { mutableStateOf(false) }

    FilledTonalButton(
        onClick = {
            when (collectionType) {
                UnifiedCollectionType.NOT_COLLECTED, UnifiedCollectionType.WISH -> onClick(UnifiedCollectionType.DONE)
                UnifiedCollectionType.DONE, UnifiedCollectionType.DROPPED -> {
                    showDropdown = true
                }

                null -> {}
                UnifiedCollectionType.DOING -> {}
                UnifiedCollectionType.ON_HOLD -> {}
            }
        },
        modifier.placeholder(collectionType == null),
        colors = if (collectionType == UnifiedCollectionType.DONE || collectionType == UnifiedCollectionType.DROPPED) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                contentColor = MaterialTheme.colorScheme.outline
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.secondary
            )
        },
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        enabled = enabled
    ) {
        when (collectionType) {
            UnifiedCollectionType.DONE -> {
                Text("已看过")
            }

            UnifiedCollectionType.DROPPED -> {
                Text("已抛弃")
            }

            else -> {
                Box(Modifier.size(16.dp)) {
                    Icon(Icons.Rounded.Add, null)
                }
                Text("看过", Modifier.padding(start = 8.dp))
            }
        }

        EditCollectionTypeDropDown(
            currentType = collectionType,
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            onSetAllEpisodesDone = null,
            onClick = {
                showDropdown = false
                onClick(it.type)
            },
            actions = ACTIONS
        )
    }
}

@Preview
@Composable
private fun PreviewEpisodeCollectionActionButton() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (entry in UnifiedCollectionType.entries) {
            Text(entry.name)
            EpisodeCollectionActionButton(entry, onClick = {})
        }

        EpisodeCollectionActionButton(null, onClick = {})
    }
}
