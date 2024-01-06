package me.him188.animationgarden.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import io.kamel.image.config.LocalKamelConfig
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.LocalContext
import moe.tlaster.precompose.PreComposeApp

/**
 * 提供 i18n 等组件, 以及背景
 */
@Composable
inline fun AniApp(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    crossinline content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }
    PreComposeApp {
        CompositionLocalProvider(
            LocalI18n provides currentBundle,
            LocalKamelConfig provides DefaultKamelConfig,
        ) {
            MaterialTheme(colorScheme) {
                Box(
                    modifier = Modifier
                        .background(AppTheme.colorScheme.background)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CommonAppScaffold(
    snackbarHost: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    clearFocus: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val currentClearFocus by rememberUpdatedState(newValue = clearFocus)

    val keyboard by rememberUpdatedState(newValue = LocalSoftwareKeyboardController.current)
    val focus = remember { FocusRequester() }
    Scaffold(
        Modifier
            .focusRequester(focus)
            .focusProperties { canFocus = false }
            .clickable(remember { MutableInteractionSource() }, null) {
                keyboard?.hide()
                focus.freeFocus()
                currentClearFocus?.invoke()
            },
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
    ) {
        content()
    }
}

@Composable
fun RowScope.TabNavigationItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.weight(1f)) {
        Box(
            modifier = modifier
                .align(Alignment.Center)
                .minimumInteractiveComponentSize()
                .fillMaxSize()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    enabled = true,
                    role = Role.Checkbox,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ),
            contentAlignment = Alignment.Center
        ) {
            val contentColor = if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalContentColor.current
            }
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    icon()
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        title()
                    }
                }
            }
        }

//        IconToggleButton(
//            colors = colors,
//            checked = checked,
//            onCheckedChange = onCheckedChange,
//            modifier = modifier.size(48.dp),
//        ) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                icon()
//                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
//                    title()
//                }
//            }
//        }
    }
}
