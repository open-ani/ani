package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context
import java.awt.Desktop
import java.net.URI

class DesktopBrowserNavigator : BrowserNavigator {
    override fun openBrowser(context: Context, url: String) {
        Desktop.getDesktop().browse(URI.create(url))
    }

    override fun openMagnetLink(context: Context, url: String) {
        Desktop.getDesktop().browse(URI.create(url))
    }
}