package me.him188.animationgarden.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.desktop.app.ApplicationState
import me.him188.animationgarden.desktop.app.doSearch
import me.him188.animationgarden.desktop.i18n.LocalI18n
import me.him188.animationgarden.desktop.i18n.ProvideResourceBundleI18n
import me.him188.animationgarden.desktop.ui.MainPage
import me.him188.animationgarden.desktop.ui.WindowEx
import java.io.File

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        application(exitProcessOnExit = true) {
            val app = remember {
                ApplicationState(AnimationGardenClient.Factory.create(), File(System.getProperty("user.dir"))).apply {
                    doSearch(null)
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

            app.saver.attachAutoSave()
            ProvideResourceBundleI18n {
//                Window(create = { ComposeWindow() }, content = {}, dispose = { it.dispose() })
                WindowEx(
                    title = LocalI18n.current.getString("title"),
                    onCloseRequest = ::exitApplication,
                    minimumSize = minimumSize,
                ) {
                    // This actually runs only once since app is never changed.
                    SideEffect {
                        this.window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                        this.window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    }

                    Box(
                        Modifier.background(color = AppTheme.colorScheme.background).padding(top = 16.dp)
                    ) { // safe area
                        MainPage(app)
                    }
                }
//
//                WindowEx(
//                    title = LocalI18n.current.getString("title"),
//                    onCloseRequest = ::exitApplication,
//                    minimumSize = minimumSize
//                ) {
//                    MainPage(app)
//                }
            }
        }
    }
}


typealias AppTheme = MaterialTheme