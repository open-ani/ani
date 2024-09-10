package me.him188.ani.app.ui.subject.episode.video.sidesheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.episode.TAG_MEDIA_SELECTOR_SHEET
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorView
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceInfoProvider
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsView
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet

@Composable
fun EpisodeVideoMediaSelectorSideSheet(
    mediaSelectorPresentation: MediaSelectorPresentation,
    mediaSourceResultsPresentation: MediaSourceResultsPresentation,
    mediaSourceInfoProvider: MediaSourceInfoProvider,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EpisodeVideoSettingsSideSheet(
        onDismissRequest = onDismissRequest,
        Modifier.testTag(TAG_MEDIA_SELECTOR_SHEET),
        title = { Text(text = "选择数据源") },
        closeButton = {
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Rounded.Close, contentDescription = "关闭")
            }
        },
    ) {
        MediaSelectorView(
            mediaSelectorPresentation,
            sourceResults = {
                MediaSourceResultsView(mediaSourceResultsPresentation, mediaSelectorPresentation)
            },
            modifier.padding(horizontal = 16.dp)
                .fillMaxWidth()
                .navigationBarsPadding(),
            stickyHeaderBackgroundColor = MaterialTheme.colorScheme.surface,
            itemProgressBar = {},
            onClickItem = {
                mediaSelectorPresentation.select(it)
                onDismissRequest()
            },
            singleLineFilter = true,
        )
    }
}
