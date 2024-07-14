@file:Suppress("UnusedReceiverParameter")

package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.subject.SubjectCollectionStats
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeButton
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeState

// 详情页内容 (不包含背景)
@Composable
fun SubjectDetailsDefaults.CollectionData(
    collectionStats: SubjectCollectionStats,
    modifier: Modifier = Modifier,
) {
    // 数据
    Row(modifier) {
        val collection = collectionStats
        Text(
            remember(collection) {
                "${collection.collect} 收藏 / ${collection.doing} 在看"
            },
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            remember(collection) {
                " / ${collection.dropped} 抛弃"
            },
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            color = LocalContentColor.current.slightlyWeaken(),
        )
    }
}

@Composable
fun SubjectDetailsDefaults.CollectionAction(
    state: EditableSubjectCollectionTypeState,
) {
    EditableSubjectCollectionTypeButton(state)
}

@Composable
fun SubjectDetailsDefaults.SelectEpisodeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text("选集播放")
    }
}