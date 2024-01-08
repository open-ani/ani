package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import me.him188.ani.app.ProvideCompositionLocalsForPreview
import me.him188.ani.app.preview.PreviewData

@Composable
@Preview(widthDp = 1080 / 2, heightDp = 2400 / 2, showBackground = true)
internal actual fun PreviewEpisodePage() {
    ProvideCompositionLocalsForPreview {
        EpisodePageContent(
            EpisodeViewModel(
                PreviewData.SOSOU_NO_FURILEN_SUBJECT_ID,
                PreviewData.SOSOU_NO_FURILEN_EPISODE_ID
            )
        )
    }
}