/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.android.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.android.AnimationGardenApplication
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.app.LocalAppSettingsManager
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.LocalAlwaysShowTitlesInSeparateLine

@Composable
fun BaseComponentActivity.CommonAppScaffold(
    topBar: @Composable () -> Unit,
    clearFocus: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val currentClearFocus by rememberUpdatedState(newValue = clearFocus)
    val appSettingsManager = remember {
        AnimationGardenApplication.instance.appSettingsManager
    }

    val context = LocalContext.current
    val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }
    CompositionLocalProvider(
        LocalI18n provides currentBundle,
        LocalAppSettingsManager provides appSettingsManager,
        LocalAlwaysShowTitlesInSeparateLine provides true,
    ) {
        val keyboard by rememberUpdatedState(newValue = LocalSoftwareKeyboardController.current)
//            Box(modifier = Modifier
//                .focusProperties { canFocus = false }
//                .clickable(remember { MutableInteractionSource() }, null) {
//                    keyboard?.hide()
//                }
//                .systemBarsPadding()
//            ) {
//                Box(modifier = Modifier.padding(vertical = 16.dp)) {
//                    MainPage(app = app, 8.dp)
//                }
//            }
        val focus = remember { FocusRequester() }
        Scaffold(
            Modifier
                .focusRequester(focus)
                .focusProperties { canFocus = false }
                .clickable(remember { MutableInteractionSource() }, null) {
                    keyboard?.hide()
                    focus.freeFocus()
                    currentClearFocus?.invoke()
                }
                .systemBarsPadding(),
            topBar = topBar,
            snackbarHost = {
                snackbarHostState.currentSnackbarData?.let {
                    Snackbar(it)
                }
            },

            ) {
            Box(
                modifier = Modifier
                    .background(AppTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(it) // padding first
            ) {
                Box(modifier = Modifier.padding(top = 0.dp)) {
                    content()
                }
            }
        }
    }
}


@Composable
fun CommonTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.colorScheme.primary,
    contentColor: Color = AppTheme.colorScheme.onPrimary,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val currentKeyboard by rememberUpdatedState(newValue = LocalSoftwareKeyboardController.current)

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        TopAppBar(
            title = title,
            actions = actions,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            navigationIcon = navigationIcon,
            modifier = modifier
                .focusProperties { canFocus = false }
                .clickable(remember { MutableInteractionSource() }, null) {
                    currentKeyboard?.hide()
                },
        )
    }
}
