package me.him188.ani.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.preference.ThemeKind
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.tools.LocalTimeFormatter
import me.him188.ani.app.tools.TimeFormatter
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.LocalImageLoader
import me.him188.ani.app.ui.foundation.getDefaultImageLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class AniAppViewModel : AbstractViewModel(), KoinComponent {
    private val settings: SettingsRepository by inject()
    val themeKind: ThemeKind? by settings.uiSettings.flow.map { it.theme.kind }.produceState(null)
}

@Composable
fun AniApp(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val coilContext = LocalPlatformContext.current
    val imageLoader by remember {
        derivedStateOf {
            getDefaultImageLoader(coilContext)
        }
    }

    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalTimeFormatter provides remember { TimeFormatter() },
    ) {
        val focusManager by rememberUpdatedState(LocalFocusManager.current)
        val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)

        val viewModel = viewModel { AniAppViewModel() }

        MaterialTheme(
            colorScheme ?: currentPlatformColorTheme(
                viewModel.themeKind ?: return@CompositionLocalProvider, // 主题读好再进入 APP, 防止黑白背景闪烁
            ),
        ) {
            Box(
                modifier = modifier
                    .focusable(false)
                    .clickable(
                        remember { MutableInteractionSource() },
                        null,
                    ) {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    },
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

@Composable
internal expect fun currentPlatformColorTheme(themeKind: ThemeKind): ColorScheme
