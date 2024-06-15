package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context

interface BrowserNavigator {
    fun openBrowser(context: Context, url: String)

    fun openJoinGroup(context: Context)

    fun openJoinTelegram(context: Context) = openBrowser(
        context,
        "https://t.me/openani"
    )

    fun openMagnetLink(context: Context, url: String)
}