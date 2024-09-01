@file:OptIn(TestOnly::class)

package me.him188.ani.app.ui.subject.episode.video.sidesheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE
import me.him188.ani.app.ui.subject.episode.mediaFetch.emptyMediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSourceInfoProvider
import me.him188.ani.utils.platform.annotations.TestOnly

@Composable
@Preview
@Preview(device = Devices.TABLET)
@Preview(device = PHONE_LANDSCAPE)
fun PreviewEpisodeVideoMediaSelectorSideSheet() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoMediaSelectorSideSheet(
            mediaSelectorPresentation = rememberTestMediaSelectorPresentation(),
            mediaSourceResultsPresentation = emptyMediaSourceResultsPresentation(),
            rememberTestMediaSourceInfoProvider(),
            onDismissRequest = {},
        )
    }
}
