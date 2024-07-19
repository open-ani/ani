package me.him188.ani.app.ui.subject.episode.details.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.cache.TestMediaList
import me.him188.ani.app.ui.subject.episode.statistics.renderProperties

@Composable
fun TestEpisodeWatchStatusButton() {
    var isDone by rememberSaveable { mutableStateOf(false) }
    EpisodeWatchStatusButton(
        isDone = isDone,
        onUnmark = { isDone = false },
        onMarkAsDone = { isDone = true },
    )
}

@Composable
@PreviewLightDark
fun PreviewPlayingEpisodeItem() = ProvideCompositionLocalsForPreview {
    val media = TestMediaList[0]
    PlayingEpisodeItem(
        episodeSort = { Text("01") },
        title = { Text("中文剧集名称") },
        watchStatus = { TestEpisodeWatchStatusButton() },
        mediaSelected = true,
        mediaLabels = { Text(media.renderProperties()) },
        filename = {
            Text("filename-".repeat(8) + ".mkv", maxLines = 3, overflow = TextOverflow.Ellipsis)
        },
        mediaSource = {
            PlayingEpisodeItemDefaults.MediaSource(media = media, true, {})
        },
        actions = {
            PlayingEpisodeItemDefaults.ActionCache({ })
            PlayingEpisodeItemDefaults.ActionShare(media)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .animateContentSize(),
    )
}

@Composable
@PreviewLightDark
fun PreviewPlayingEpisodeItemNotSelected() = ProvideCompositionLocalsForPreview {
    PlayingEpisodeItem(
        episodeSort = { Text("01") },
        title = { Text("中文剧集名称") },
        watchStatus = { TestEpisodeWatchStatusButton() },
        mediaSelected = false,
        mediaLabels = { },
        filename = {
            Text("filename-".repeat(8) + ".mkv", maxLines = 3, overflow = TextOverflow.Ellipsis)
        },
        mediaSource = {
            var isLoading by remember { mutableStateOf(false) }
            PlayingEpisodeItemDefaults.MediaSource(
                media = null, isLoading = isLoading,
                onClick = { isLoading = !isLoading },
                modifier = Modifier.clickable {
                    isLoading = !isLoading
                },
            )
        },
        actions = {
            PlayingEpisodeItemDefaults.ActionCache({ })
            PlayingEpisodeItemDefaults.ActionShare(null)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .animateContentSize(),
    )
}
