package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context

interface BrowserNavigator {
    fun openBrowser(context: Context, url: String): Boolean

    fun openMagnetLink(context: Context, url: String): Boolean
}