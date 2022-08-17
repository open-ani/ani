package me.him188.animationgarden.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.desktop.app.ApplicationState
import me.him188.animationgarden.desktop.i18n.LocalI18n
import me.him188.animationgarden.desktop.i18n.ProvideResourceBundleI18n
import me.him188.animationgarden.desktop.ui.MainPage
import me.him188.animationgarden.desktop.ui.WindowEx

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        application(exitProcessOnExit = true) {
            val app = remember {
                ApplicationState(AnimationGardenClient.Factory.create()).apply {
                    launchFetchNextPage(false)
                }
            }
            val currentDensity by rememberUpdatedState(LocalDensity.current)
            val minimumSize by remember {
                derivedStateOf {
                    with(currentDensity) {
                        Size(200.dp.toPx(), 200.dp.toPx())
                    }
                }
            }

            ProvideResourceBundleI18n {
                WindowEx(
                    title = LocalI18n.current.getString("title"),
                    onCloseRequest = ::exitApplication,
                    minimumSize = minimumSize
                ) {
                    MainPage(app)
                }
            }
        }
    }
}


typealias AppTheme = MaterialTheme