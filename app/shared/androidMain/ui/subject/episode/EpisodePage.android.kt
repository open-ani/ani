package me.him188.ani.app.ui.subject.episode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import me.him188.ani.app.ProvideCompositionLocalsForPreview

@Composable
@Preview(widthDp = 1080 / 2, heightDp = 2400 / 2, showBackground = true)
internal actual fun PreviewEpisodePage() {
    ProvideCompositionLocalsForPreview {
        EpisodePageContent(
            EpisodeViewModel(
                424663,
                1277147
            )
        )
    }
}