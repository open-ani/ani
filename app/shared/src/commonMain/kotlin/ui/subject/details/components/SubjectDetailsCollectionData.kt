@file:Suppress("UnusedReceiverParameter")

package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.subject.SubjectCollectionStats
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.subject.collection.progress.PlaySubjectButton
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState

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
fun SubjectDetailsDefaults.SelectEpisodeButton(
    state: SubjectProgressState,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Box(Modifier.weight(1f)) {
            PlaySubjectButton(state, Modifier.fillMaxWidth())
        }
    }
}