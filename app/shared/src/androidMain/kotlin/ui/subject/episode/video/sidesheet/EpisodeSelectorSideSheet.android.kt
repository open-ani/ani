package me.him188.ani.app.ui.subject.episode.video.sidesheet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.icons.PlayingIcon
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.coroutines.EmptyCoroutineContext

@Composable
@Preview
@Preview(device = Devices.TABLET)
@Preview(device = PHONE_LANDSCAPE)
fun PreviewEpisodeSelectorSideSheet() {
    ProvideCompositionLocalsForPreview {
        EpisodeSelectorSideSheet(
            state = remember {
                EpisodeSelectorState(
                    MutableStateFlow(
                        listOf(
                            EpisodePresentation(
                                episodeId = -1,
                                title = "placeholder",
                                ep = "placeholder",
                                sort = "01",
                                collectionType = UnifiedCollectionType.WISH,
                                isKnownBroadcast = true,
                                isPlaceholder = true,
                            ),
                            EpisodePresentation(
                                episodeId = 1,
                                title = "placeholder",
                                ep = "placeholder",
                                sort = "02",
                                collectionType = UnifiedCollectionType.WISH,
                                isKnownBroadcast = true,
                                isPlaceholder = true,
                            ),
                            EpisodePresentation(
                                episodeId = 2,
                                title = "placeholder",
                                ep = "placeholder",
                                sort = "03",
                                collectionType = UnifiedCollectionType.WISH,
                                isKnownBroadcast = false,
                                isPlaceholder = true,
                            ),
                        ),
                    ),
                    MutableStateFlow(1),
                    {},
                    EmptyCoroutineContext,
                )
            },
            onDismissRequest = {},
        )
    }
}


@Preview
@Composable
fun PreviewPlayingIcon() {
    ProvideCompositionLocalsForPreview {
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.border(1.dp, color = Color.Magenta)) {
                PlayingIcon("正在播放")
            }
        }
    }
}