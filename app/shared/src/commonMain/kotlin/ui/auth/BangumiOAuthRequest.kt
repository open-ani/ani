package me.him188.ani.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 类似 Dialog, 用于请求 Bangumi OAuth 授权.
 *
 * - On Android: An embedded WebView
 * - On Desktop: A http server and opens a browser window
 */
@Composable
expect fun BangumiOAuthRequest(
    onUpdateCode: (String) -> Unit,
    modifier: Modifier = Modifier,
)