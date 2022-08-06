package me.him188.animationgarden.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.desktop.i18n.LocalI18n
import me.him188.animationgarden.desktop.i18n.ResourceBundle
import me.him188.animationgarden.desktop.ui.ApplicationState
import me.him188.animationgarden.desktop.ui.MainPage
import me.him188.animationgarden.desktop.ui.WindowEx

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        application(exitProcessOnExit = true) {
            val app = remember {
                ApplicationState(AnimationGardenClient.Factory.create()).apply {
                    launchFetchNextPage()
                }
            }
            val currentDensity by rememberUpdatedState(LocalDensity.current)
            val minimumSize by remember {
                derivedStateOf {
                    with(currentDensity) {
                        Size(320.dp.toPx(), (320 / 9 * 16).dp.toPx())
                    }
                }
            }

            val currentBundle = remember(Locale.current.language) { ResourceBundle.load() }
            CompositionLocalProvider(LocalI18n provides currentBundle) {
                WindowEx(
                    title = LocalI18n.current.getString("title"),
                    onCloseRequest = ::exitApplication,
                    minimumSize = minimumSize
                ) {
                    Box {
                        MainPage(app)
                    }
                }
            }
        }
    }
}


typealias AppTheme = MaterialTheme