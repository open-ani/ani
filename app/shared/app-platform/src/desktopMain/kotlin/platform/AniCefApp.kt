/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.platform

import com.jetbrains.cef.JCefAppConfig
import io.ktor.http.Url
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.utils.logging.*
import me.him188.ani.utils.platform.currentPlatform
import me.him188.ani.utils.platform.currentTimeMillis
import me.him188.ani.utils.platform.isMacOS
import org.cef.CefApp
import org.cef.CefClient
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.callback.CefAuthCallback
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.misc.CefLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

object AniCefApp {
    private val logger = logger<AniCefApp>()

    @Volatile
    private var app: CefApp? = null

    private val lock = Mutex()

    private var proxyServer: Url? = null
    private var proxyAuthUsername: String? = null
    private var proxyAuthPassword: String? = null

    private val proxiedRequestHandler = object : CefRequestHandlerAdapter() {
        override fun getAuthCredentials(
            browser: CefBrowser?,
            originUrl: String?,
            isProxy: Boolean,
            host: String?,
            port: Int,
            realm: String?,
            scheme: String?,
            callback: CefAuthCallback?,
        ): Boolean {
            if (!isProxy) return false
            if (host != proxyServer?.host) return false
            if (port != proxyServer?.port) return false
            if (scheme != proxyServer?.protocol?.name) return false

            if (callback == null) return false
            callback.Continue(proxyAuthUsername, proxyAuthPassword)
            return true
        }
    }

    /**
     * Create a new [CefApp].
     *
     * Note that you must terminate the last instance before creating new one.
     * Otherwise it will return the existing instance.
     */
    // not thread-safe
    private fun createCefApp(
        logDir: File,
        cacheDir: File,
        proxyServer: String? = null,
    ): CefApp {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        val jcefConfig = JCefAppConfig.getInstance()

        jcefConfig.cefSettings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_DEFAULT
        jcefConfig.cefSettings.log_file = logDir
            .resolve("cef-${dateFormat.format(Date(currentTimeMillis()))}.log")
            .absolutePath
        jcefConfig.cefSettings.windowless_rendering_enabled = true
        jcefConfig.cefSettings.cache_path = cacheDir.absolutePath

        jcefConfig.appArgsAsList.apply {
            add("--mute-audio")
            add("--force-dark-mode")

            proxyServer?.let { add("--proxy-server=${it}") }
        }

        if (currentPlatform().isMacOS()) {
            // Fix framework paths when packaged
            jcefConfig.appArgsAsList.apply {
                removeAll { it.startsWith("--framework-dir-path") }
                removeAll { it.startsWith("--browser-subprocess-path") }
                removeAll { it.startsWith("--main-bundle-path") }
                findMacOsFrameworkPath()?.let {
                    // See `JCefAppConfig.getInstance` sources for why we use these paths.
                    logger.info { "Found CEF framework at $it" }
                    add("--framework-dir-path=${it.resolve("Chromium Embedded Framework.framework")}")
                    add("--browser-subprocess-path=${it.resolve("jcef Helper.app/Contents/MacOS/jcef Helper")}")
                    add("--main-bundle-path=${it.resolve("jcef Helper.app")}")
                } ?: logger.error { "CEF framework not found" }
            }
        }

        CefLog.init(jcefConfig.cefSettings)
        CefApp.startup(jcefConfig.appArgs)
        return CefApp.getInstance(jcefConfig.appArgs, jcefConfig.cefSettings)
    }

    /**
     * Returns `$JAVA_HOME/../Frameworks`
     */
    private fun findMacOsFrameworkPath(): File? {
        /*
        Absolute path: /Users/him188/Projects/ani/app/desktop/build/compose/binaries/main-release/app/Ani.app/Contents
        user.dir/Users/him188/Projects/ani/app/desktop/build/compose/binaries/main-release/app/Ani.app/Contents
        Java home: /Users/him188/Projects/ani/app/desktop/build/compose/binaries/main-release/app/Ani.app/Contents/runtime/Contents/Home
         */
        logger.info { "Absolute path: " + File(".").normalize().absolutePath }
        logger.info { "user.dir: " + File(System.getProperty("user.dir")).normalize().absolutePath }
        logger.info { "Java home: " + System.getProperty("java.home") }

        val javaHome = File(System.getProperty("java.home"))
        sequence {
            yield(javaHome.resolve("../Frameworks/Chromium Embedded Framework.framework"))
            yield(javaHome.resolve("Frameworks/Chromium Embedded Framework.framework"))
            yield(javaHome.resolve("lib/Frameworks/Chromium Embedded Framework.framework"))
        }.forEach {
            if (it.exists()) {
                return it.parentFile.normalize()
            }
        }

        return null
    }

    /**
     * Initialize singleton instance of [CefApp].
     *
     * You can call [getInstance] later to get it.
     */
    suspend fun initialize(
        logDir: File,
        cacheDir: File,
        proxyServer: String? = null,
        proxyAuthUsername: String? = null,
        proxyAuthPassword: String? = null,
    ) {
        val currentApp = app
        if (currentApp != null) return

        lock.withLock {
            val currentApp2 = app
            if (currentApp2 != null) return

            val newApp = suspendCoroutineOnCefContext {
                createCefApp(logDir, cacheDir, proxyServer)
            }
            this.proxyServer = proxyServer?.let(::Url)
            this.proxyAuthUsername = proxyAuthUsername
            this.proxyAuthPassword = proxyAuthPassword

            Runtime.getRuntime().addShutdownHook(
                thread(start = false) {
                    blockOnCefContext { newApp.dispose() }
                },
            )
            app = newApp
            logger.info { "AniCefApp is initialized." }
        }
    }

    /**
     * Create a new CEF client.
     *
     * You should dispose it if drop.
     *
     * @return `null` if CefApp hasn't initialized yet.
     */
    fun createClient(): CefClient? {
        return app?.createClient()
            ?.apply { addRequestHandler(proxiedRequestHandler) }
    }

    /**
     * You should always call cef methods in Cef context.
     */
    fun runOnCefContext(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeLater(block)
        }
    }

    /**
     * You should always call cef methods in Cef context.
     */
    fun blockOnCefContext(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeAndWait(block)
        }
    }

    /**
     * Run in Cef context and get result.
     */
    suspend fun <T> suspendCoroutineOnCefContext(block: () -> T): T {
        return suspendCancellableCoroutine {
            runOnCefContext {
                it.resumeWith(runCatching(block))
            }
        }
    }
}