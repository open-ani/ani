package me.him188.animationgarden.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.him188.animationgarden.desktop.ui.MainPage

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        application {
            Window(onCloseRequest = ::exitApplication) {
                MainPage()
            }
        }
    }
}
