/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.test

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.source.media.resolver.TestWebViewVideoExtractor
import me.him188.ani.app.data.source.media.source.web.SelectorSearchConfig
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.settings.mediasource.rss.test.buildMatchTags
import me.him188.ani.app.ui.settings.mediasource.selector.edit.rememberTestSelectorConfigurationState
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.coroutines.EmptyCoroutineContext

@TestOnly
private val configurationContent: @Composable ColumnScope.(contentPadding: PaddingValues) -> Unit = { contentPadding ->
    SelectorEpisodePaneDefaults.ConfigurationContent(
        rememberTestSelectorConfigurationState(),
        contentPadding = contentPadding,
    )
}

@OptIn(TestOnly::class)
@Composable
@Preview
fun PreviewSelectorEpisodePaneWithBottomSheet() = ProvideFoundationCompositionLocalsForPreview {
    Surface {
        SelectorEpisodePane(
            state = rememberTestSelectorEpisodeState(
                TestSelectorTestEpisodePresentations[0],
                SelectorSearchConfig.MatchVideoConfig(),
            ),
            layout = SelectorEpisodePaneLayout.WithBottomSheet,
            configurationContent = configurationContent,
        )
    }
}

@OptIn(TestOnly::class)
@Composable
@Preview
fun PreviewSelectorEpisodePaneListOnly() {
    ProvideFoundationCompositionLocalsForPreview {
        Surface {
            SelectorEpisodePane(
                state = rememberTestSelectorEpisodeState(),
                layout = SelectorEpisodePaneLayout.ListOnly,
                configurationContent = configurationContent,
            )
        }
    }
}

@TestOnly
@Stable
internal val TestSelectorTestEpisodePresentations
    get() = listOf(
        SelectorTestEpisodePresentation(
            name = "Test Episode 2",
            episodeSort = EpisodeSort(2),
            playUrl = "https://example.com",
            tags = buildMatchTags {
                emit("EP: 02", isMatch = true)
                emit("https://example.com", isMatch = true)
            },
            origin = null,
        ),
        SelectorTestEpisodePresentation(
            name = "Test Episode Unknown",
            episodeSort = null,
            playUrl = "https://example.com",
            tags = buildMatchTags {
                emit("缺失 EP", isMissing = true)
            },
            origin = null,
        ),
    )

@TestOnly
@Composable
internal fun rememberTestSelectorEpisodeState(
    item: SelectorTestEpisodePresentation? = TestSelectorTestEpisodePresentations[0],
    config: SelectorSearchConfig.MatchVideoConfig = SelectorSearchConfig.MatchVideoConfig(),
    urls: (pageUrl: String) -> List<String> = {
        listOf("https://example.com/a.mkv")
    },
): SelectorEpisodeState {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    return remember {
        SelectorEpisodeState(
            itemState = stateOf(item),
            matchVideoConfigState = stateOf(config),
            webViewVideoExtractor = stateOf(TestWebViewVideoExtractor(urls)),
            engine = TestSelectorMediaSourceEngine(),
            backgroundScope = scope,
            flowDispatcher = EmptyCoroutineContext,
            context = context,
        )
    }
}
