package me.him188.animationgarden.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.model.SearchFilter
import me.him188.animationgarden.desktop.ui.ApplicationState
import me.him188.animationgarden.desktop.ui.MainPage

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        application {
            val app = remember {
                ApplicationState(AnimationGardenClient.Factory.create(), SearchFilter())
            }
            Window(onCloseRequest = ::exitApplication) {
                MainPage(app)
            }
        }
    }
}
