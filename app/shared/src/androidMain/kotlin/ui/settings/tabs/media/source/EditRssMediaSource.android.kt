package me.him188.ani.app.ui.settings.tabs.media.source

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.data.source.media.source.RssMediaSourceEngine
import me.him188.ani.app.tools.rss.RssParser
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourcePage
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourceState
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPaneState
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.topic.toTopicCriteria
import me.him188.ani.utils.platform.annotations.TestOnly
import me.him188.ani.utils.xml.Xml

@TestOnly
internal object TestRssMediaSourceEngine : RssMediaSourceEngine() {
    private val parsed by lazy {
        Xml.parse(
            """
                <rss version="2.0">
                <channel>
                <title>樱trick</title>
                <description>Anime Garden 是動漫花園資源網的第三方镜像站, 動漫花園資訊網是一個動漫愛好者交流的平台,提供最及時,最全面的動畫,漫畫,動漫音樂,動漫下載,BT,ED,動漫遊戲,資訊,分享,交流,讨论.</description>
                <link>https://garden.breadio.wiki/resources?page=1&pageSize=100&search=%5B%22%E6%A8%B1trick%22%5D</link>
                <item>
                <title>[愛戀&漫猫字幕社]櫻Trick Sakura Trick 01-12 avc_flac mkv 繁體內嵌合集(急招時軸)</title>
                <link>https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42</link>
                <guid isPermaLink="true">https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42</guid>
                <pubDate>Sat, 18 Nov 2023 04:54:02 GMT</pubDate>
                <enclosure url="magnet:?xt=urn:btih:d22868eee2dae4214476ac865e0b6ec533e09e57" length="0" type="application/x-bittorrent"/>
                </item>
            """.trimIndent(),
        )
    }

    override suspend fun searchImpl(
        finalUrl: Url,
        query: DownloadSearchQuery,
        page: Int?,
        mediaSourceId: String
    ): ApiResponse<Result> {
        return runApiRequest {
            val channel = RssParser.parse(parsed, includeOrigin = true)

            Result(
                finalUrl,
                query,
                parsed,
                channel,
                channel.items.mapNotNull { convertItemToMedia(it, mediaSourceId, query.toTopicCriteria()) },
            )
        }
    }
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
fun PreviewEditRssMediaSourcePagePhone() = ProvideCompositionLocalsForPreview {
    val (edit, test) = rememberTestEditRssMediaSourceStateAndRssTestPaneState()
    EditRssMediaSourcePage(edit, test)
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
fun PreviewEditRssMediaSourcePagePhoneTest() = ProvideCompositionLocalsForPreview {
    val navigator = rememberListDetailPaneScaffoldNavigator()
    val (edit, test) = rememberTestEditRssMediaSourceStateAndRssTestPaneState()
    EditRssMediaSourcePage(
        edit, test,
        navigator = navigator,
    )
    SideEffect {
        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }
}

@OptIn(TestOnly::class)
@Composable
@Preview(device = Devices.PIXEL_TABLET)
@Preview(device = Devices.PIXEL_TABLET, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
fun PreviewEditRssMediaSourcePageLaptop() = ProvideCompositionLocalsForPreview {
    val (edit, test) = rememberTestEditRssMediaSourceStateAndRssTestPaneState()
    EditRssMediaSourcePage(edit, test)
}

@TestOnly
@Composable
internal fun rememberTestEditRssMediaSourceStateAndRssTestPaneState(): Pair<EditRssMediaSourceState, RssTestPaneState> {
    val scope = rememberCoroutineScope()
    val edit = rememberTestEditRssMediaSourceState(scope)
    return edit to remember {
        RssTestPaneState(
            derivedStateOf { edit.searchUrl },
            TestRssMediaSourceEngine,
            scope,
        )
    }
}

@TestOnly
@Composable
internal fun rememberTestEditRssMediaSourceState(scope: CoroutineScope) = remember {
    EditRssMediaSourceState(
        arguments = RssMediaSourceArguments.Default,
        editMediaSourceMode = EditMediaSourceMode.Add,
        instanceId = "test-id",
        onSave = {},
        backgroundScope = scope,
    )
}
