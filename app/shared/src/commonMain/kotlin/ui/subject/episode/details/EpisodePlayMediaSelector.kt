package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorSourceResults
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorView

/**
 * 播放视频时的选择数据源
 */
@Composable
fun EpisodePlayMediaSelector(
    state: MediaSelectorPresentation,
    sourceResults: MediaSelectorSourceResults,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaSelectorView(
        state,
        modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .navigationBarsPadding(),
        actions = {
            TextButton(onDismissRequest) {
                Text("取消")
            }
        },
        sourceResults = sourceResults,
    )
}