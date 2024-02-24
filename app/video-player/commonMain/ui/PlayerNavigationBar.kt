package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.AniTopAppBar

/**
 * 播放器顶部导航栏
 */
@Composable
fun PlayerNavigationBar(
    actions: @Composable (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
) {
    AniTopAppBar(
        modifier
            .fillMaxWidth(),
        actions = actions,
        containerColor = Color.Transparent,
        padding = PaddingValues(
            start = 4.dp,
            top = 2.dp,
            end = 4.dp,
            bottom = 2.dp
        )
    )
}