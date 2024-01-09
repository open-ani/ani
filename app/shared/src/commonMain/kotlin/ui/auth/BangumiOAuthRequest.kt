package me.him188.ani.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BangumiOAuthRequest(
    onUpdateCode: (String) -> Unit,
    modifier: Modifier = Modifier,
)