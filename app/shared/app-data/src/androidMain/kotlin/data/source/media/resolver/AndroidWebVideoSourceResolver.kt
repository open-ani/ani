/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.resolver

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.videoplayer.HttpStreamingVideoSource
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import me.him188.ani.datasources.api.matcher.WebVideoMatcherContext
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import java.io.ByteArrayInputStream

/**
 * 用 WebView 加载网站, 拦截 WebView 加载资源, 用各数据源提供的 [WebVideoMatcher]
 */
class AndroidWebVideoSourceResolver : VideoSourceResolver {
    private val matchers by lazy {
        java.util.ServiceLoader.load(WebVideoMatcher::class.java, this::class.java.classLoader).filterNotNull()
    }

    override suspend fun supports(media: Media): Boolean = media.download is ResourceLocation.WebVideo

    private var attached: Context? = null

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    override fun ComposeContent() {
        super.ComposeContent()

        val context = LocalContext.current
        DisposableEffect(true) {
            attached = context
            onDispose {
                attached = null
            }
        }
    }

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        if (!supports(media)) throw UnsupportedMediaException(media)
        val matcherContext = WebVideoMatcherContext(media)
        val webVideo = WebViewVideoExtractor().getVideoResourceUrl(
            attached ?: throw IllegalStateException("WebVideoSourceResolver not attached"),
            media.download.uri,
            resourceMatcher = {
                matchers.firstNotNullOfOrNull { matcher ->
                    matcher.match(it, matcherContext)
                }
            },
        )
        return HttpStreamingVideoSource(webVideo.m3u8Url, media.originalTitle, webVideo = webVideo, media.extraFiles)
    }
}

class WebViewVideoExtractor {
    private companion object {
        private val logger = logger<WebViewVideoExtractor>()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetJavaScriptEnabled")
    suspend fun <R : Any> getVideoResourceUrl(
        context: Context,
        pageUrl: String,
        resourceMatcher: (String) -> R?,
    ): R {
        val deferred = CompletableDeferred<R>()
        withContext(Dispatchers.Main) {
            val webView = WebView(context)
            deferred.invokeOnCompletion {
                GlobalScope.launch(Dispatchers.Main.immediate) {
                    webView.destroy()
                }
            }

            webView.settings.javaScriptEnabled = true

            webView.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    if (request == null) return null
                    val url = request.url ?: return super.shouldInterceptRequest(view, request)
                    if (url.toString().contains(".mp4")) {
                        logger.info { "Found url: $url" }
                    }
                    val matched = resourceMatcher(url.toString())
                    if (matched != null) {
                        logger.info { "Found video resource via shouldInterceptRequest: $url" }
                        deferred.complete(matched)

                        // 拦截, 以防资源只能加载一次
                        return WebResourceResponse(
                            "text/plain",
                            "UTF-8", 500,
                            "Internal Server Error",
                            mapOf(),
                            ByteArrayInputStream(ByteArray(0)),
                        )
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }
            webView.loadUrl(pageUrl)
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
