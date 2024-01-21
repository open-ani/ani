package me.him188.ani.android.navigation

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.utils.logging.logger

class AndroidBrowserNavigator : BrowserNavigator {
    private val logger = logger(this::class)

    override fun openBrowser(context: Context, url: String): Boolean {
        return runCatching {
            launchChromeTab(context, url)
        }.recoverCatching {
            view(url, context)
        }.onFailure {
            logger.warn("Failed to open tab", it)
        }.isSuccess
    }

    private fun launchChromeTab(context: Context, url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    }

    override fun openMagnetLink(context: Context, url: String): Boolean {
        return kotlin.runCatching {
            view(url, context)
        }.onFailure {
            logger.warn("Failed to open browser", it)
        }.isSuccess
    }

    private fun view(url: String, context: Context) {
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            setData(Uri.parse(url))
        }
        context.startActivity(browserIntent)
    }
}