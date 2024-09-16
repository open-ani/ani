/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.components.comment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout.LayoutParams
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.platform.annotation.Language
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toaster

// TODO: move to compose resources
@Language("HTML")
private const val TURNSTILE_HTML_CONTENT = """
<!DOCTYPE html>
<html lang="en">
<head>
    <title>trunstile_to_bgm_next_api</title>
    <script src="https://challenges.cloudflare.com/turnstile/v0/api.js?onload=onLoadTurnstile" defer></script>
</head>
<body>
    <main>
        <div id="turnstile-container"></div>
    </main>
</body>
<script>
    window.onLoadTurnstile = function () {
        turnstile.render('#turnstile-container', {
            sitekey: '${'$'}{SITE_KEY}',
            theme: '${'$'}{THEME}',
            callback: function(token) { alert(token); },
        });
    };
</script>
</html>
"""

@Stable
class AndroidTurnstileState(
    override val siteKey: String,
): TurnstileState {
    private val webViewState = mutableStateOf<WebView?>(null)
    var webView: WebView?
        get() = webViewState.value
        set(value) {
            if (webViewState.value == value) return
            webViewState.value = value
            value?.applySettings()
            value?.restoreOrLoadPage()
        }
    
    private val client = TurnstileWebClient()
    private val callbackTokenChannel = Channel<String>()
    private val chromeClient = TurnstileWebChromeClient { alert ->
        if (alert == null) return@TurnstileWebChromeClient
        callbackTokenChannel.trySend(alert)
    }
    
    var isDarkTheme: Boolean = false
    // WebView 重新创建的时候会使用此 state bundle 恢复状态
    var webViewStateBundle: Bundle? = null

    override val tokenFlow: Flow<String>
        get() = callbackTokenChannel.receiveAsFlow()
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.applySettings() {
        settings.apply {
            setSupportZoom(false)
            displayZoomControls = false
            builtInZoomControls = false
            javaScriptEnabled = true
            setBackgroundColor(Color.TRANSPARENT)
        }

        webViewClient = client
        webChromeClient = chromeClient
    }
    
    private fun WebView.reloadPage() {
        loadData(
            TURNSTILE_HTML_CONTENT
                .replace("\${SITE_KEY}", siteKey)
                .replace("\${THEME}", if (isDarkTheme) "dark" else "light"),
            null,
            null
        )
    }
    
    private fun WebView.restoreOrLoadPage() {
        val currentState = webViewStateBundle
        if (currentState != null) {
            restoreState(currentState)
            return
        }
        reloadPage()
    }

    override fun reload() {
        webView?.reloadPage()
    }
}

actual fun createTurnstileState(siteKey: String): TurnstileState {
    return AndroidTurnstileState(siteKey)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun ActualTurnstile(
    state: TurnstileState,
    constraints: Constraints,
    modifier: Modifier,
) {
    check(state is AndroidTurnstileState)
    val isDark = isSystemInDarkTheme()
    val currentLayoutParams = remember(constraints) {
        LayoutParams(
            if (constraints.hasFixedWidth) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT,
            if (constraints.hasFixedHeight) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
        )
    }
    
    AndroidView(
        factory = { context -> 
            WebView(context)
                .apply { layoutParams = currentLayoutParams }
                .also { 
                    state.isDarkTheme = isDark
                    state.webView = it 
                }
        },
        modifier = modifier,
        update = { webView ->
            webView.layoutParams = currentLayoutParams
            state.isDarkTheme = isDark
            state.webView = webView
        }
    )
}

private class TurnstileWebClient : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }
}

private class TurnstileWebChromeClient(
    private val onAlert: (message: String?) -> Unit
) : WebChromeClient() {
    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        onAlert(message)
        result?.confirm()
        return true
    }
}

/**
 * This preview is only available when running at real device.
 */
@Preview
@Composable
fun PreviewBangumi38DevTurnstile() {
    ProvideFoundationCompositionLocalsForPreview {
        val context =  LocalContext.current
        CompositionLocalProvider(
            LocalToaster provides object : Toaster {
                override fun toast(text: String) {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                }
            }
        ) {
            val toaster = LocalToaster.current
            val state = remember { TurnstileState(siteKey = "1x00000000000000000000AA") }
            
            LaunchedEffect(Unit) {
                state.tokenFlow.collectLatest {
                    toaster.toast("get token: $it")
                }
            }
            
            Column {
                Turnstile(
                    state = state,
                    modifier = Modifier
                )
                Row { 
                    Button({ state.reload() }) { Text("Reload") }
                }
            }
        }
    }
}