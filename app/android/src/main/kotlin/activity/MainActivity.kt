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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import me.him188.animationgarden.android.AnimationGardenApplication
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.ui.home.HomePage

class MainActivity : BaseComponentActivity() {
    private val starredListActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val app = AnimationGardenApplication.instance.app
        }

    private var updateAppliedKeyword: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            ObserveSettingsChanges()


            MaterialTheme(colorScheme) {
                ImmerseStatusBar(AppTheme.colorScheme.background)

                MainPage()
            }
        }
    }


    @Composable
    private fun MainPage() {
        CommonAppScaffold {
            HomePage()
//            AndroidMainPage(app, focus)
        }
    }

    @Composable
    private fun ObserveSettingsChanges() {
        val appSettingsManager = remember {
            AnimationGardenApplication.instance.appSettingsManager
        }
        appSettingsManager.attachAutoSave()

        val currentAppSettings by rememberUpdatedState(newValue = appSettingsManager.value.collectAsState().value)
        LaunchedEffect(currentAppSettings.proxy) {
            // proxy changed, update client
//            app.client.value = withContext(Dispatchers.IO) {
//                DmhyClient.Factory.create {
//                    proxy = currentAppSettings.proxy.toKtorProxy()
//                }
//            }
        }

    }
}
