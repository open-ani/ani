package me.him188.ani.app.ui.profile

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.ktor.http.encodeURLParameter
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getAniUserAgent

@Composable
actual fun BangumiOAuthRequest(
    onComplete: (String) -> Unit,
    onFailed: (Throwable) -> Unit,
    modifier: Modifier,
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        val urlString = request.url.toString()
                        if (urlString.startsWith("ani://bangumi-oauth-callback")) {
                            onComplete(request.url.getQueryParameter("code") ?: "")
                            return null
                        }
                        if (urlString.startsWith("https://bgm.tv/oauth/authorize")) {
                            return null
                        }
                        // forbid others
                        return WebResourceResponse("text/plain", "UTF-8", null)
                    }
                }
                settings.userAgentString = getAniUserAgent(currentAniBuildConfig.versionName)
            }
        },
        modifier,
        update = {
            it.loadUrl(
                "https://bgm.tv/oauth/authorize" +
                        "?client_id=${currentAniBuildConfig.bangumiOauthClientId}" +
                        "&response_type=code" +
                        "&redirect_uri=" + "ani://bangumi-oauth-callback".encodeURLParameter()
            )
        },
        onRelease = {
            it.destroy()
        }
    )
}