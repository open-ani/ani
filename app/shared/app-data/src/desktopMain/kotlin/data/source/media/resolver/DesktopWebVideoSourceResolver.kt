/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.resolver

import io.github.bonigarcia.wdm.WebDriverManager
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.util.date.toJvmDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.preference.ProxyConfig
import me.him188.ani.app.data.models.preference.VideoResolverSettings
import me.him188.ani.app.data.models.preference.WebViewDriver
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.resolver.WebViewVideoExtractor.Instruction
import me.him188.ani.app.platform.Context
import me.him188.ani.app.videoplayer.HttpStreamingVideoSource
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.matcher.MediaSourceWebVideoMatcherLoader
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.matcher.WebViewConfig
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openqa.selenium.Cookie
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.HasDevTools
import org.openqa.selenium.devtools.v125.network.Network
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.safari.SafariDriver
import org.openqa.selenium.safari.SafariOptions
import java.util.Optional
import java.util.function.Consumer
import java.util.logging.Level

/**
 * 用 WebView 加载网站, 拦截 WebView 加载资源, 用各数据源提供的 [WebVideoMatcher]
 */
class DesktopWebVideoSourceResolver(
    private val matcherLoader: MediaSourceWebVideoMatcherLoader
) : VideoSourceResolver, KoinComponent {
    private companion object {
        private val logger = logger<DesktopWebVideoSourceResolver>()
    }

    private val matchersFromClasspath by lazy {
        java.util.ServiceLoader.load(WebVideoMatcher::class.java).filterNotNull()
    }
    private val settings: SettingsRepository by inject()

    override suspend fun supports(media: Media): Boolean = media.download is ResourceLocation.WebVideo

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        return withContext(Dispatchers.Default) {
            if (!supports(media)) throw UnsupportedMediaException(media)

            val config = settings.proxySettings.flow.first().default
            val resolverSettings = settings.videoResolverSettings.flow.first()
            val matchersFromMediaSource = matcherLoader.loadMatchers(media.mediaSourceId)
            val allMatchers = matchersFromMediaSource + matchersFromClasspath

            val webViewConfig = allMatchers.fold(WebViewConfig.Empty) { acc, matcher ->
                matcher.patchConfig(acc)
            }
            logger.info { "Final config: $webViewConfig" }


            val context = WebVideoMatcherContext(media)
            fun match(url: String): WebVideoMatcher.MatchResult? {
                return allMatchers
                    .asSequence()
                    .map { matcher ->
                        matcher.match(url, context)
                    }
                    .firstOrNull { it !is WebVideoMatcher.MatchResult.Continue }
            }

            val webVideo = SeleniumWebViewVideoExtractor(config.config.takeIf { config.enabled }, resolverSettings)
                .getVideoResourceUrl(
                    media.download.uri,
                    webViewConfig,
                    resourceMatcher = {
                        when (match(it)) {
                            WebVideoMatcher.MatchResult.Continue -> Instruction.Continue
                            WebVideoMatcher.MatchResult.LoadPage -> Instruction.LoadPage
                            is WebVideoMatcher.MatchResult.Matched -> Instruction.FoundResource
                            null -> Instruction.Continue
                        }
                    },
                )?.let {
                    (match(it.url) as? WebVideoMatcher.MatchResult.Matched)?.video
                } ?: throw VideoSourceResolutionException(ResolutionFailures.NO_MATCHING_RESOURCE)
            return@withContext HttpStreamingVideoSource(
                webVideo.m3u8Url,
                media.originalTitle,
                webVideo = webVideo,
                media.extraFiles,
            )
        }
    }
}

class SeleniumWebViewVideoExtractor(
    private val proxyConfig: ProxyConfig?,
    private val videoResolverSettings: VideoResolverSettings,
) : WebViewVideoExtractor {
    private companion object {
        private val logger = logger<WebViewVideoExtractor>()

        init {
            // disable logs
            System.setProperty("webdriver.chrome.silentOutput", "true")
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF)
            java.util.logging.Logger.getLogger("org.apache.hc.client5.http.wire").setLevel(Level.OFF)
            java.util.logging.Logger.getLogger("org.openqa.selenium.devtools.Connection").setLevel(Level.OFF)
        }
    }


    override suspend fun getVideoResourceUrl(
        context: Context,
        pageUrl: String,
        config: WebViewConfig,
        resourceMatcher: (String) -> Instruction
    ): WebResource? = getVideoResourceUrl(pageUrl, config, resourceMatcher)

    suspend fun getVideoResourceUrl(
        pageUrl: String,
        webViewConfig: WebViewConfig,
        resourceMatcher: (String) -> Instruction,
    ): WebResource? {
        val deferred = CompletableDeferred<WebResource>()

        return try {
            withContext(Dispatchers.IO) {
                logger.info { "Starting Selenium to resolve video source from $pageUrl" }

                val driver: RemoteWebDriver = createDriver()

                /**
                 * @return if the url has been consumed
                 */
                fun handleUrl(url: String): Boolean {
                    val matched = resourceMatcher(url)
                    when (matched) {
                        Instruction.Continue -> return false
                        Instruction.FoundResource -> {
                            deferred.complete(WebResource(url))
                            return true
                        }

                        Instruction.LoadPage -> {
                            if (driver.currentUrl == url) return false // don't recurse
                            logger.info { "WebView loading nested page: $url" }
                            val script = "window.location.href = '$url'"
                            logger.info { "WebView executing: $script" }
                            driver.executeScript(script)
//                        decoratedDriver.get(url)
                            return false
                        }
                    }
                }

                logger.info { "Using WebDriver: $driver" }

                check(driver is HasDevTools) {
                    "WebDriver must support DevTools"
                }
                val devTools = driver.devTools
                devTools.createSession()
//            devTools.send(Network.enable(Optional.of(10000000), Optional.of(10000000), Optional.of(10000000)))
                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))
                devTools.addListener(
                    Network.requestWillBeSent(),
                    Consumer {
                        val url = it.request.url
                        if (handleUrl(url)) {
                            logger.info { "Found video resource via devtools: $url" }
                        }
                    },
                )
                devTools.send(Network.clearBrowserCache())

                deferred.invokeOnCompletion {
                    @Suppress("OPT_IN_USAGE")
                    GlobalScope.launch {
                        kotlin.runCatching {
                            driver.quit()
                        }.onFailure {
                            logger.error(it) { "Failed to close selenium" }
                        }
                    }
                }

                driver.get(pageUrl)

                for (t in webViewConfig.cookies) {
                    try {
                        val url = Url(pageUrl)
                        driver.manage().addCookie(
                            parseServerSetCookieHeader(t)
                                .toDriverCookie(domain = url.host),
                        )
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse or add cookie, see cause" }
                    }
                }
            }

            deferred.await()
        } catch (e: Throwable) {
            if (deferred.isActive) {
                deferred.cancel() // will quit driver
            }
            throw e
        }
    }

    private fun io.ktor.http.Cookie.toDriverCookie(domain: String): Cookie {
        return Cookie(name, value, domain, path, expires?.toJvmDate(), secure, httpOnly)
    }

    private fun createDriver(): RemoteWebDriver {
        return getPreferredDriverFactory()
            .runCatching {
                create(videoResolverSettings, proxyConfig)
            }
            .let {
                // 依次尝试备用
                var result = it
                for (fallback in getFallbackDrivers()) {
                    result = result.recoverCatching {
                        fallback.create(videoResolverSettings, proxyConfig)
                    }
                }
                result
            }
            .getOrThrow()
        // TODO: update user settings if we fell back to a different driver
    }


    private fun getPreferredDriverFactory(): WebDriverFactory {
        return when (videoResolverSettings.driver) {
            WebViewDriver.CHROME -> WebDriverFactory.Chrome
            WebViewDriver.EDGE -> WebDriverFactory.Edge
            else -> WebDriverFactory.Chrome
        }
    }

    private fun getFallbackDrivers(): List<WebDriverFactory> {
        return listOf(
            WebDriverFactory.Chrome,
            WebDriverFactory.Edge,
        )
    }
}

private sealed interface WebDriverFactory {
    fun create(videoResolverSettings: VideoResolverSettings, proxyConfig: ProxyConfig?): RemoteWebDriver

    data object Edge : WebDriverFactory {
        override fun create(videoResolverSettings: VideoResolverSettings, proxyConfig: ProxyConfig?): RemoteWebDriver {
            WebDriverManager.edgedriver().setup()
            return EdgeDriver(
                EdgeOptions().apply {
                    if (videoResolverSettings.headless) {
                        addArguments("--headless")
                        addArguments("--disable-gpu")
                    }
//                addArguments("--log-level=3")
                    proxyConfig?.let {
                        addArguments("--proxy-server=${it.url}")
                    }
                },
            )
        }
    }

    data object Chrome : WebDriverFactory {
        override fun create(videoResolverSettings: VideoResolverSettings, proxyConfig: ProxyConfig?): RemoteWebDriver {
            WebDriverManager.chromedriver().setup()
            return ChromeDriver(
                ChromeOptions().apply {
                    if (videoResolverSettings.headless) {
                        addArguments("--headless")
                        addArguments("--disable-gpu")
                    }
//                addArguments("--log-level=3")
                    proxyConfig?.let {
                        addArguments("--proxy-server=${it.url}")
                    }
                },
            )
        }
    }

    @Deprecated("Safari is not supported")
    data object Safari : WebDriverFactory {
        /**
         * SafariDriver does not support the use of proxies.
         * https://github.com/SeleniumHQ/selenium/issues/10401#issuecomment-1054814944
         */ // 而且还要求用户去设置里开启开发者模式
        override fun create(videoResolverSettings: VideoResolverSettings, proxyConfig: ProxyConfig?): RemoteWebDriver {
            WebDriverManager.safaridriver().setup()
            return SafariDriver(
                SafariOptions().apply {
                    proxyConfig?.let {
                        // Causes an exception
                        setCapability("proxy", it.url)
                    }
                },
            )
        }
    }
}
