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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.animationgarden.android.AnimationGardenApplication
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.R
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.app.toKtorProxy
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.MainPage
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            ObserveSettingsChanges()

            MaterialTheme {
                ImmerseStatusBar(AppTheme.colorScheme.primary)

                MainPage()
            }
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    @Composable
    private fun MainPage() {
        CommonAppScaffold(topBar = {
            MainTopBar()
        }) {
            val app = remember {
                AnimationGardenApplication.instance.app
            }
            MainPage(app = app, 8.dp)
        }
    }

    @Composable
    private fun ObserveSettingsChanges() {
        val appSettingsManager = remember {
            AnimationGardenApplication.instance.appSettingsManager
        }
        appSettingsManager.attachAutoSave()

        val app = remember {
            AnimationGardenApplication.instance.app
        }

        val currentAppSettings by rememberUpdatedState(newValue = appSettingsManager.value.value)
        LaunchedEffect(currentAppSettings.proxy) {
            // proxy changed, update client
            app.client.value = withContext(Dispatchers.IO) {
                AnimationGardenClient.Factory.create {
                    proxy = currentAppSettings.proxy.toKtorProxy()
                }
            }
        }

    }


    @Preview
    @Composable
    private fun PreviewMainTopAppBar() {
        ProvideCompositionLocalsForPreview {
            MainTopBar()
        }
    }

    @Composable
    private fun MainTopBar() {
        CommonTopAppBar(
            title = {
                Text(text = stringResource(id = R.string.app_name))
            },
            actions = {
                val context = LocalContext.current
                IconButton(onClick = { startActivity(PreferencesActivity.getIntent(context)) }) {
                    Icon(
                        Icons.Default.Settings,
                        LocalI18n.current.getString("menu.settings")
                    )
                }
            }
        )
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
