/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.resolver

import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.util.date.toJvmDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.him188.ani.app.data.models.preference.ProxyConfig
import me.him188.ani.app.data.models.preference.VideoResolverSettings
import me.him188.ani.app.data.models.preference.configIfEnabledOrNull
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.resolver.WebViewVideoExtractor.Instruction
import me.him188.ani.app.data.source.media.resolver.cef.RequestWillBeSent
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.DesktopContext
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
import me.him188.ani.utils.logging.warn
import me.him188.ani.utils.platform.currentTimeMillis
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefDevToolsClient
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/**
 * 用 WebView 加载网站, 拦截 WebView 加载资源, 用各数据源提供的 [WebVideoMatcher]
 */
class DesktopWebVideoSourceResolver(
    private val context: DesktopContext,
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

            val webVideo = CefVideoExtractor(config.configIfEnabledOrNull, resolverSettings)
                .getVideoResourceUrl(
                    this@DesktopWebVideoSourceResolver.context,
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

class CefVideoExtractor(
    private val proxyConfig: ProxyConfig?,
    private val videoResolverSettings: VideoResolverSettings,
) : WebViewVideoExtractor {
    private companion object {
        private val logger = logger<WebViewVideoExtractor>()
        private val json = Json { ignoreUnknownKeys = true }
    }

    override suspend fun getVideoResourceUrl(
        context: Context,
        pageUrl: String,
        config: WebViewConfig,
        resourceMatcher: (String) -> Instruction
    ): WebResource? = withContext(Dispatchers.IO) {
        val cefApp = AniCefApp.getInstance() ?: kotlin.run { 
            logger.warn { "AniCefApp isn't initialized yet." }
            return@withContext null
        }
        val client = cefApp.createClient()

        val deferred = CompletableDeferred<WebResource>()
        val browser = client.createBrowser("about:blank", true, true)
        browser.setCloseAllowed() // browser should be allowed to close.
        
        try {
            val devToolsDeferred = CompletableDeferred<CefDevToolsClient>()

            AniCefApp.runOnCefContext {
                client.addLoadHandler(object : CefLoadHandlerAdapter() {
                    override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                        if (frame?.isMain == true && browser != null) {
                            devToolsDeferred.complete(browser.devToolsClient)
                        }
                    }
                })
                client.addDisplayHandler(
                    object : CefDisplayHandlerAdapter() {
                        override fun onConsoleMessage(
                            browser: CefBrowser?,
                            level: CefSettings.LogSeverity?,
                            message: String?,
                            source: String?,
                            line: Int
                        ): Boolean {
                            logger.info { "CEF client console: ${message?.replace("\n", "\\n")} ($source:$line)" }
                            return super.onConsoleMessage(browser, level, message, source, line)
                        }
                    },
                )

                // start browser immediately
                browser.createImmediately()
            }
            // 加载 data URL 不会耗费太长时间, 和设备的性能有关
            val devTools = withTimeout(5000L) { devToolsDeferred.await() }

            fun handleUrl(url: String): Boolean {
                val matched = resourceMatcher(url)
                when (matched) {
                    Instruction.Continue -> return false
                    Instruction.FoundResource -> {
                        deferred.complete(WebResource(url))
                        return true
                    }

                    Instruction.LoadPage -> {
                        if (browser.url == url) return false // don't recurse
                        logger.info { "CEF loading nested page: $url" }
                        AniCefApp.runOnCefContext { browser.loadURL(url) }
                        return false
                    }
                }
            }

            AniCefApp.runOnCefContext {
                // 获取到 DevTools 后就可以移除 load handler 了
                browser.client.removeLoadHandler()
                
                // set cookie
                val cookieManager = CefCookieManager.getGlobalManager()
                val url = Url(pageUrl)
                for (cookie in config.cookies) {
                    val ktorCookie = parseServerSetCookieHeader(cookie)
                    cookieManager.setCookie(url.host, ktorCookie.toCefCookie())
                }

                // setup network
                devTools.executeDevToolsMethod("Network.enable")
                devTools.addEventListener { name, message ->
                    if (name != "Network.requestWillBeSent") return@addEventListener
                    val data = json.decodeFromString(RequestWillBeSent.serializer(), message)

                    if (handleUrl(data.request.url)) {
                        logger.info { "Found video resource via DevTools: ${data.request.url}" }
                    }
                }
                devTools.executeDevToolsMethod("Network.clearBrowserCache")

                logger.info { "Fetching $pageUrl" }
                // load real page after setting up DevTools
                browser.loadURL(pageUrl)
            }
            
            deferred.await()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to get video url." }
            if (deferred.isActive) {
                deferred.cancel()
            }
            null
        } finally {
            // close browser and client asynchronously.
            AniCefApp.runOnCefContext {
                browser.close(true)
                client.dispose()
            }
            logger.info { "CEF client is disposed." }
        }
    }
}

private fun Cookie.toCefCookie() =
    CefCookie(
        name, 
        value, 
        domain, 
        path, 
        secure, 
        httpOnly, 
        null,
        null,
        expires != null,
        expires?.toJvmDate()
    )

private object AniCefApp {
    @Volatile
    private var app: CefApp? = null
    
    private val lock = Mutex()

    /**
     * Create a new [CefApp].
     * 
     * Note that you must terminate the last instance before creating new one.
     * Otherwise it will return the existing instance.
     */
    // not thread-safe
    private fun createCefApp(
        logDir: File, 
        cacheDir: File,
        proxyConfig: ProxyConfig?
    ): CefApp {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return CefAppBuilder().apply {
            setInstallDir(File("cef"))
            cefSettings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_DISABLE
            cefSettings.log_file = logDir
                .resolve("cef_${dateFormat.format(Date(currentTimeMillis()))}.log")
                .absolutePath
            cefSettings.windowless_rendering_enabled = true
            cefSettings.root_cache_path = cacheDir.absolutePath
            addJcefArgs(
                *buildList {
                    add("--disable-gpu")
                    add("--mute-audio")
                    add("--force-dark-mode")
                    proxyConfig?.let { add("--proxy-server=${it.url}") }
                }.toTypedArray()
            )

            setAppHandler(object : MavenCefAppHandlerAdapter() {
                override fun stateHasChanged(state: CefAppState?) {
                    if (state == CefAppState.TERMINATED) {
                        // cef app has shutdown, we need to initialize a new one while getting instance.
                        app = null
                    }
                }
            })
        }.build()
    }

    /**
     * Initialize singleton instance of [CefApp]. You can call [getInstance] later to get it.
     */
    suspend fun instance(
        logDir: File,
        cacheDir: File,
        proxyConfig: ProxyConfig?
    ): CefApp {
        val currentApp = app
        if (currentApp != null) return currentApp

        lock.withLock {
            val currentApp2 = app
            if (currentApp2 != null) return currentApp2
            
            val newApp = suspendCoroutineOnCefContext { createCefApp(logDir, cacheDir, proxyConfig) }
            
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                blockOnCefContext { newApp.dispose() }
            })
            app = newApp

            return newApp
        }
    }

    /**
     * Get singleton instance of [CefApp].
     * 
     * @return `null` if it hasn't initialized yet.
     */
    fun getInstance(): CefApp? {
        return app
    }

    /**
     * You should always call cef methods in Cef context.
     */
    fun runOnCefContext(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeLater(block)
        }
    }

    /**
     * You should always call cef methods in Cef context.
     */
    fun blockOnCefContext(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeAndWait(block)
        }
    }

    /**
     * Run in Cef context and get result.
     */
    suspend fun <T> suspendCoroutineOnCefContext(block: () -> T): T {
        return suspendCancellableCoroutine { 
            runOnCefContext {
                it.resumeWith(runCatching(block))
            }
        }
    }
}