package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelector
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorSourceResults
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorState
import me.him188.ani.datasources.api.Media

/**
 * 选择数据源来缓存
 * @param onSelect 当用户点击一个资源时调用
 * @param onCancel 当用户点击"取消"时调用
 */
@Composable
fun EpisodeCacheMediaSelector(
    state: MediaSelectorState,
    onSelect: (Media) -> Unit,
    onCancel: () -> Unit,
    sourceResults: MediaSelectorSourceResults,
    modifier: Modifier = Modifier,
) {
    MediaSelector(
        state,
        modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        onClickItem = {
            onSelect(it)
        },
        actions = {
            OutlinedButton(onCancel) {
                Text("取消")
            }
        },
        sourceResults = sourceResults,
    )
}