package me.him188.ani.app.videoplayer.ui.top

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import me.him188.ani.app.navigation.LocalBackHandler
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme

/**
 * 播放器顶部导航栏
 */
@Composable
fun PlayerTopBar(
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
                val focusManager by rememberUpdatedState(LocalFocusManager.current) // workaround for #288
                IconButton(
                    onClick = { back.onBackPress() },
                    Modifier.ifThen(needWorkaroundForFocusManager) {
                        onFocusEvent {
                            if (it.hasFocus) {
                                focusManager.clearFocus()
                            }
                        }
                    },
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        actions = {
            CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground) {
                actions()
            }
        },
    )
}

// See #288
@Stable
val needWorkaroundForFocusManager: Boolean get() = Platform.currentPlatform.isDesktop()