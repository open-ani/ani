package me.him188.ani.app.data.media.resolver

import io.github.bonigarcia.wdm.WebDriverManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.ProxyConfig
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.HttpStreamingVideoSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.HasDevTools
import org.openqa.selenium.devtools.NetworkInterceptor
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.http.HttpHandler
import org.openqa.selenium.remote.http.HttpMethod
import org.openqa.selenium.remote.http.HttpResponse
import org.openqa.selenium.remote.http.Route
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariOptions
import org.openqa.selenium.support.events.EventFiringDecorator
import org.openqa.selenium.support.events.WebDriverListener
import java.lang.reflect.Method
import java.util.logging.Level

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

            val webVideo = SeleniumWebViewVideoExtractor(config.config.takeIf { config.enabled })
                .getVideoResourceUrl(
                    media.download.uri,
                    resourceMatcher = {
                        matchers.firstNotNullOfOrNull { matcher ->
                            matcher.match(it, WebVideoMatcherContext(media))
                        }
                    },
                )
            return@withContext HttpStreamingVideoSource(
                webVideo.m3u8Url,
                media.originalTitle,
                webVideo = webVideo,
                media.extraFiles,
            )
        }
    }
}

interface WebViewVideoExtractor {
    suspend fun <R : Any> getVideoResourceUrl(
        pageUrl: String,
        resourceMatcher: (String) -> R?
    ): R
}


class SeleniumWebViewVideoExtractor(
    private val proxyConfig: ProxyConfig?
) : WebViewVideoExtractor {
    private companion object {
        private val logger = logger<WebViewVideoExtractor>()

        init {
            // disable logs
            System.setProperty("webdriver.chrome.silentOutput", "true")
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF)
            java.util.logging.Logger.getLogger("org.apache.hc.client5.http.wire").setLevel(Level.OFF)
        }
    }

    private fun createChromeDriver(): ChromeDriver {
        WebDriverManager.chromedriver().setup()
        return ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
                addArguments("--disable-gpu")
                proxyConfig?.let {
                    addArguments("--proxy-server=${it.url}")
                }
            },
        )
    }

    private fun createEdgeDriver(): EdgeDriver {
        WebDriverManager.edgedriver().setup()
        return EdgeDriver(
            EdgeOptions().apply {
                addArguments("--headless")
                addArguments("--disable-gpu")
                proxyConfig?.let<ProxyConfig, Unit> {
                    addArguments("--proxy-server=${it.url}")
                }
            },
        )
    }

    private fun createSafariDriver(): SafariDriver {
        WebDriverManager.safaridriver().setup()
        return SafariDriver(
            SafariOptions().apply {
                proxyConfig?.let {
                    setCapability("proxy", it.url)
                }
            },
        )
    }

    override suspend fun <R : Any> getVideoResourceUrl(
        pageUrl: String,
        resourceMatcher: (String) -> R?
    ): R {
        val deferred = CompletableDeferred<R>()

        withContext(Dispatchers.IO) {
            logger.info { "Starting Selenium with Edge to resolve video source from $pageUrl" }

            val driver: RemoteWebDriver = when (Platform.currentPlatform) {
                is Platform.Linux -> throw UnsupportedOperationException("Linux is not supported")
                is Platform.MacOS -> {
                    createChromeDriver()
//                    kotlin.runCatching {
//                        createChromeDriver()
//                    }.recoverCatching {
//                        createSafariDriver()
//                    }.getOrThrow()
                }

                is Platform.Windows -> {
                    kotlin.runCatching {
                        createChromeDriver()
                    }.recoverCatching {
                        createEdgeDriver()
                    }.getOrThrow()
                }

                Platform.Android -> error("Unexpected Android platform in desktop builds")
            }

            logger.info { "Using WebDriver: $driver" }

            val listener = object : WebDriverListener {
                override fun beforeAnyNavigationCall(
                    navigation: WebDriver.Navigation?,
                    method: Method?,
                    args: Array<out Any>?
                ) {
                    logger.info { "Navigating to $pageUrl" }
                }

                override fun beforeGet(driver: WebDriver?, url: String?) {
                    if (driver == null || url == null) return
                    resourceMatcher(url)?.let { matched ->
                        logger.info { "Found video resource via beforeGet: $url" }
                        deferred.complete(matched)
                    }
                }
            }

            check(driver is HasDevTools) {
                "WebDriver must support DevTools"
            }
            val decoratedDriver: WebDriver =
                EventFiringDecorator(WebDriver::class.java, listener).decorate(driver)
            val emptyResponseHandler = HttpHandler { _ ->
                HttpResponse().apply {
                    status = 500
                }
            }

            val route: Route = Route.matching { req ->
                if (HttpMethod.GET != req.method) return@matching false

                val url = req.uri
                val matched = resourceMatcher(url)
                if (matched != null) {
                    logger.info { "Found video resource via network interception: $url" }
                    deferred.complete(matched)
                    return@matching true
                }
                false
            }.to { emptyResponseHandler }

            val interceptor = NetworkInterceptor(driver, route)
            deferred.invokeOnCompletion {
                @Suppress("OPT_IN_USAGE")
                GlobalScope.launch {
                    kotlin.runCatching {
                        interceptor.close()
                        decoratedDriver.quit()
                    }.onFailure {
                        logger.error(it) { "Failed to close selenium" }
                    }
                }
            }


            decoratedDriver.navigate().to(pageUrl)
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