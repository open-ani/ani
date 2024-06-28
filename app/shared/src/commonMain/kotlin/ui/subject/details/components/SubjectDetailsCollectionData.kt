package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.subject.collection.CollectionActionButton
import me.him188.ani.app.ui.subject.collection.EditCollectionTypeDropDown
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

// 详情页内容 (不包含背景)
@Composable
fun SubjectDetailsCollectionData(
    info: SubjectInfo,
    selfCollectionType: UnifiedCollectionType,
    onClickSelectEpisode: () -> Unit,
    onSetAllEpisodesDone: () -> Unit,
    onSetCollectionType: (UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp
) {
    // 收藏数据和收藏按钮
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 数据
        Row(Modifier.fillMaxWidth().padding(horizontal = horizontalPadding)) {
            val collection = info.collection
            Text(
                remember(collection) {
                    "${collection.collect} 收藏 / ${collection.wish} 想看 / ${collection.doing} 在看"
                },
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                remember(collection) {
                    " / ${collection.onHold} 搁置 / ${collection.dropped} 抛弃"
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = LocalContentColor.current.slightlyWeaken(),
            )
        }

        Row(
            Modifier
                .align(Alignment.End)
                .padding(horizontal = horizontalPadding),
        ) {
            // 收藏按钮
            if (selfCollectionType != UnifiedCollectionType.NOT_COLLECTED) {
                TextButton(onClickSelectEpisode) {
                    Text("选集播放")
                }

                var showDropdown by remember { mutableStateOf(false) }
                EditCollectionTypeDropDown(
                    currentType = selfCollectionType,
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    onSetAllEpisodesDone = onSetAllEpisodesDone,
                    onClick = {
                        showDropdown = false
                        onSetCollectionType(it.type)
                    },
                )
                CollectionActionButton(
                    type = selfCollectionType,
                    onCollect = { onSetCollectionType(UnifiedCollectionType.DOING) },
                    onEdit = onSetCollectionType,
                    onSetAllEpisodesDone = onSetAllEpisodesDone,
                )
            } else {
                CollectionActionButton(
                    type = selfCollectionType,
                    onCollect = { onSetCollectionType(UnifiedCollectionType.DOING) },
                    onEdit = onSetCollectionType,
                    onSetAllEpisodesDone = onSetAllEpisodesDone,
                )
            }
        }
    }
}
