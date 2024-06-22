package me.him188.ani.app.ui.subject.episode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Composable
@Preview(widthDp = 1080 / 3, heightDp = 2400 / 3, showBackground = true)
@Preview(device = Devices.TABLET, showBackground = true)
internal actual fun PreviewEpisodePage() {
    ProvideCompositionLocalsForPreview {
        val context = LocalContext.current
        EpisodePage(
            remember {
                EpisodeViewModel(
                    424663,
                    1277147,
                    context = context,
                )
            },
        )
    }
}