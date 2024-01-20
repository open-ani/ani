package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.videoplayer.rememberPlayerController

@Composable
@Preview(widthDp = 1080 / 2, heightDp = 2400 / 2, showBackground = true)
internal actual fun PreviewEpisodePage() {
    ProvideCompositionLocalsForPreview {
        EpisodePageContent(
            EpisodeViewModel(
                424663,
                1277147
            ),
            playerController = rememberPlayerController(videoSource = flowOf())
        )
    }
}