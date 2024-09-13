package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.runtime.Immutable
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.tools.rss.RssChannel
import me.him188.ani.datasources.api.Media
import me.him188.ani.utils.xml.Element

@Immutable
sealed class RssTestResult { // for ui
    @Immutable
    data object EmptyResult : RssTestResult()

    @Immutable
    data class Success(
        val encodedUrl: String,
        val channel: RssChannel,
        val rssItems: List<RssItemPresentation>,
        val mediaList: List<Media>,
        val origin: Element?,
    ) : RssTestResult() {
        val originString = origin.toString()
    }

    sealed class Failed : RssTestResult()

    @Immutable
    data class ApiError(
        val reason: ApiFailure,
    ) : Failed()

    @Immutable
    data class UnknownError(
        val exception: Throwable,
    ) : Failed()
}
