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

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.animationgarden.android.AnimationGardenApplication
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.R
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.app.LocalAppSettingsManager
import me.him188.animationgarden.app.app.toKtorProxy
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.LocalAlwaysShowTitlesInSeparateLine
import me.him188.animationgarden.app.ui.MainPage
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setImmerseStatusBarSystemUiVisibility()

        setContent {
            MaterialTheme {
                MainPage()
            }
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    private fun MainPage() {
        val appSettingsManager = remember {
            AnimationGardenApplication.instance.appSettingsManager
        }
        val app = remember {
            AnimationGardenApplication.instance.app
        }

        appSettingsManager.attachAutoSave()

        val currentAppSettings by rememberUpdatedState(appSettingsManager.value.value)

        LaunchedEffect(currentAppSettings.proxy) {
            // proxy changed, update client
            app.client.value = withContext(Dispatchers.IO) {
                AnimationGardenClient.Factory.create {
                    proxy = currentAppSettings.proxy.toKtorProxy()
                }
            }
        }

        val context = LocalContext.current
        val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }
        CompositionLocalProvider(
            LocalI18n provides currentBundle,
            LocalAppSettingsManager provides appSettingsManager,
            LocalAlwaysShowTitlesInSeparateLine provides true,
        ) {
            val keyboard by rememberUpdatedState(newValue = LocalSoftwareKeyboardController.current)
            val snackbarHostState = remember { SnackbarHostState() }
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
            Scaffold(
                Modifier
                    .focusProperties { canFocus = false }
                    .clickable(remember { MutableInteractionSource() }, null) {
                        keyboard?.hide()
                    }
                    .systemBarsPadding(),
                topBar = {
//                    MainTopAppBar()
                },
                snackbarHost = {
                    snackbarHostState.currentSnackbarData?.let {
                        Snackbar(it)
                    }
                },

                ) {
                Box(modifier = Modifier.padding(it)) {
                    Box(modifier = Modifier.padding(top = 0.dp)) {
                        MainPage(app = app, 8.dp)
                    }
                }
            }
        }
    }

    @Composable
    private fun MainTopAppBar() {
        val context = this
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.app_name))

            },
            actions = {
                IconButton(onClick = { startActivity(PreferencesActivity.getIntent(context)) }) {
                    Icon(
                        Icons.Default.Settings,
                        LocalI18n.current.getString("menu.settings")
                    )
                }
            },
        )
    }

    @Preview
    @Composable
    private fun PreviewMainTopAppBar() {
        ProvideCompositionLocalsForPreview {
            MainTopAppBar()
        }
    }
}

@Preview
@Composable
private fun PreviewMainPage() {
    ProvideCompositionLocalsForPreview {
        val app = remember {
            ApplicationState(AnimationGardenClient.Factory.create {}, File("."))
        }
        MaterialTheme {
            MainPage(app, innerPadding = 8.dp)
        }
    }
}
