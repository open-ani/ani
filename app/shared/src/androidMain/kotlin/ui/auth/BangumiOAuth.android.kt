package me.him188.ani.app.ui.auth

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.ktor.http.encodeURLParameter
import me.him188.ani.BuildConfig

@Composable
actual fun BangumiOAuthRequest(
    onUpdateCode: (String) -> Unit,
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
                        if (request.url.toString().startsWith("ani://bangumi-oauth-callback")) {
                            onUpdateCode(request.url.getQueryParameter("code") ?: "")
                            return null
                        }
                        return null
                    }
                }
                settings.userAgentString =
                    "him188/ani/${BuildConfig.VERSION_NAME} (Android) (https://github.com/Him188/ani)"
            }
        },
        modifier,
        update = {
            it.loadUrl(
                "https://bgm.tv/oauth/authorize" +
                        "?client_id=${BuildConfig.BANGUMI_OAUTH_CLIENT_ID}" +
                        "&response_type=code" +
                        "&redirect_uri=" + "ani://bangumi-oauth-callback".encodeURLParameter()
            )
        },
        onRelease = {
            it.destroy()
        }
    )
}