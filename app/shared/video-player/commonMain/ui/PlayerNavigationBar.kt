package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.him188.ani.app.navigation.LocalBackHandler
import me.him188.ani.app.ui.theme.aniDarkColorTheme

/**
 * 播放器顶部导航栏
 */
@Composable
fun PlayerNavigationBar(
    title: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    color: Color = aniDarkColorTheme().onBackground,
) {
    TopAppBar(
        title = {
            CompositionLocalProvider(LocalContentColor provides color) {
                if (title != null) {
                    title()
                }
            }
        },
        modifier
            .fillMaxWidth(),
        navigationIcon = {
            val back = LocalBackHandler.current
            CompositionLocalProvider(LocalContentColor provides color) {
                IconButton(onClick = { back.onBackPress() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        actions = actions,
    )
}