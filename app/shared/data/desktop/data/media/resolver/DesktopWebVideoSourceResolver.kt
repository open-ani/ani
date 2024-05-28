package me.him188.ani.app.data.media.resolver

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.ProxyConfig
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.HttpStreamingVideoSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * 用 WebView 加载网站, 拦截 WebView 加载资源, 用各数据源提供的 [WebVideoMatcher]
 */
class DesktopWebVideoSourceResolver : VideoSourceResolver, KoinComponent {
    private val matchers by lazy {
        java.util.ServiceLoader.load(WebVideoMatcher::class.java).filterNotNull()
    }
    private val settings: SettingsRepository by inject()

    override suspend fun supports(media: Media): Boolean = media.download is ResourceLocation.WebVideo

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        return withContext(Dispatchers.Default) {
            if (!supports(media)) throw UnsupportedMediaException(media)

            val config = settings.proxySettings.flow.first().default

            val matcherContext = WebVideoMatcherContext(media)
            val webVideo = WebViewVideoExtractor(config.config.takeIf { config.enabled })
                .getVideoResourceUrl(
                    media.download.uri,
                    resourceMatcher = {
                        matchers.firstNotNullOfOrNull { matcher ->
                            matcher.match(it, matcherContext)
                        }
                    }
                )
            return@withContext HttpStreamingVideoSource(webVideo.m3u8Url, media.originalTitle, webVideo = webVideo)
        }
    }
}

class WebViewVideoExtractor(
    private val proxyConfig: ProxyConfig?,
) {
    private companion object {
        private val logger = logger<WebViewVideoExtractor>()
    }

    suspend fun <R : Any> getVideoResourceUrl(
        pageUrl: String,
        resourceMatcher: (String) -> R?
    ): R {
        val deferred = CompletableDeferred<R>()

        withContext(Dispatchers.IO) {
            logger.info {
                "Starting Playwright browser to resolve video source from $pageUrl"
            }
            val playwright = Playwright.create(Playwright.CreateOptions().apply {
                env = mapOf(
//                    "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD" to "firefox,webkit"
                )
            })
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .apply {
                        proxyConfig?.let { setProxy(it.url) }
                    }
            )
            val page = browser.newPage()
            page.onRequest { request ->
                val url = request.url()
                val matched = resourceMatcher(url)
                if (matched != null) {
                    logger.info {
                        "Found video resource via network interception: $url"
                    }
                    deferred.complete(matched)
                }
            }

            page.navigate(pageUrl)

            deferred.invokeOnCompletion {
                page.close()
                browser.close()
                playwright.close()
            }
        }

        return try {
            deferred.await()
        } catch (e: Throwable) {
            if (deferred.isActive) {
                deferred.cancel()
            }
            throw e
        }
    }
}
