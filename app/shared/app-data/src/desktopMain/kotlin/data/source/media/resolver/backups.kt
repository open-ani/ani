/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.resolver


//class JavafxWebViewVideoExtractor(
//    private val proxyConfig: ProxyConfig?
//) : WebViewVideoExtractor {
//
//    private companion object {
//        private val logger = logger<WebViewVideoExtractor>()
//    }
//
//    override suspend fun <R : Any> getVideoResourceUrl(
//        pageUrl: String,
//        resourceMatcher: (String) -> R?
//    ): R {
//        val deferred = CompletableDeferred<R>()
//
//        // Initialize JavaFX runtime
//
//        withContext(Dispatchers.IO) {
//            logger.info { "Starting JavaFX WebView to resolve video source from $pageUrl" }
//            Platform.startup {} // Ensures JavaFX environment is initialized
//
//            Platform.runLater {
//                try {
//                    val webView = WebView()
//                    val webEngine: WebEngine = webView.engine
//
//
//                    webEngine.isJavaScriptEnabled = true
//                    webEngine.onStatusChangedProperty().addListener { _, _, status ->
//                        logger.info { "Status changed: $status" }
//                    }
//
//                    val javaApp = JavaApp(resourceMatcher, deferred)
//                    webEngine.loadWorker.exceptionProperty().addListener { observable, oldValue, newValue ->
//                        if (newValue is IOException && newValue.message?.startsWith("could not connect to media") == true) {
//                            logger.info { "Failed to load page: $pageUrl" }
//                            val url = newValue.message.toString().substringAfter("could not connect to media (")
//                                .substringBeforeLast(")")
//                            javaApp.intercept(url)
//                        }
//                    }
//                    // Set up a listener to intercept network requests
//                    webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
//                        logger.info { "Page: $pageUrl, state=$newState" }
//                        if (newState == Worker.State.SUCCEEDED || newState == Worker.State.RUNNING) {
//                            logger.info { "Page loaded: $pageUrl" }
//
//                            val window = webEngine.executeScript("window") as JSObject
//                            window.setMember("JavaApp", javaApp)
//                            webEngine.executeScript(
//                                """
//                                (function() {
//                                    // Intercept fetch
//                                    const originalFetch = window.fetch;
//                                    window.fetch = function() {
//                                        // Notify Java application about the fetch request
//                                        window.JavaApp.intercept(arguments[0]);
//                                        
//                                        // Proceed with the original fetch
//                                        return originalFetch.apply(this, arguments);
//                                    };
//
//                                    // Intercept XMLHttpRequest
//                                    const originalXhrOpen = XMLHttpRequest.prototype.open;
//                                    XMLHttpRequest.prototype.open = function() {
//                                        this.addEventListener('loadstart', function() {
//                                            // Notify Java application about the XMLHttpRequest
//                                            window.JavaApp.intercept(arguments[1]);
//                                        });
//                                        
//                                        // Proceed with the original open
//                                        return originalXhrOpen.apply(this, arguments);
//                                    };
//                                })();
//                                """.trimIndent()
//                            )
//                        }
//                    }
//
//                    webEngine.load(pageUrl)
//                } catch (e: Throwable) {
//                    deferred.completeExceptionally(e)
//                }
//            }
//        }
//
//        return try {
//            deferred.await()
//        } catch (e: Throwable) {
//            if (deferred.isActive) {
//                deferred.cancel()
//            }
//            throw e
//        }
//    }
//
//    class JavaApp<R : Any>(
//        private val resourceMatcher: (String) -> R?,
//        private val deferred: CompletableDeferred<R>,
//    ) {
//        @Suppress("unused") // called by js
//        fun intercept(url: String): Boolean {
//            logger.info { "Intercepted network request: $url" }
//            val matched = resourceMatcher(url)
//            if (matched != null) {
//                logger.info { "Found video resource via network interception: $url" }
//                deferred.complete(matched)
//                return true
//            }
//            return false
//        }
//    }
//}
//
//class HtmlunitWebViewVideoExtractor(
//    private val proxyConfig: ProxyConfig?
//) {
//    private companion object {
//        private val logger = logger<WebViewVideoExtractor>()
//    }
//
//    suspend fun <R : Any> getVideoResourceUrl(
//        pageUrl: String,
//        resourceMatcher: (String) -> R?
//    ): R {
//        val deferred = CompletableDeferred<R>()
//
//        withContext(Dispatchers.IO) {
//            logger.info {
//                "Starting HtmlUnit browser to resolve video source from $pageUrl"
//            }
//
//            val webClient = WebClient().apply {
//                if (proxyConfig != null) {
//                    val url = Url(proxyConfig.url)
//                    options.setProxyConfig(
//                        com.gargoylesoftware.htmlunit.ProxyConfig(
//                            url.host, url.port, url.protocol.name, url.protocol.name.contains("socks"),
//                        )
//                    )
//                }
//                options.isThrowExceptionOnScriptError = false
//                options.isCssEnabled = false
//                options.isJavaScriptEnabled = true
//                options.isThrowExceptionOnScriptError = false
//
//                webConnection = object : FalsifyingWebConnection(this) {
//                    override fun getResponse(request: WebRequest): WebResponse {
//                        val url = request.url.toString()
//                        val matched = resourceMatcher(url)
//                        if (matched != null) {
//                            logger.info {
//                                "Found video resource via network interception: $url"
//                            }
//                            deferred.complete(matched)
//                            return StringWebResponse("", request.url)
//                        }
//                        return super.getResponse(request)
//                    }
//                }
//            }
//
//            val page = webClient.getPage<HtmlPage>(pageUrl)
//            webClient.waitForBackgroundJavaScript(5000) // Wait for JavaScript to load
//
//            deferred.invokeOnCompletion {
//                webClient.close()
//            }
//        }
//
//        return try {
//            deferred.await()
//        } catch (e: Throwable) {
//            if (deferred.isActive) {
//                deferred.cancel()
//            }
//            throw e
//        }
//    }
//}
//
//class PlaywrightWebViewVideoExtractor(
//    private val proxyConfig: ProxyConfig?,
//) {
//    private companion object {
//        private val logger = logger<WebViewVideoExtractor>()
//    }
//
//    suspend fun <R : Any> getVideoResourceUrl(
//        pageUrl: String,
//        resourceMatcher: (String) -> R?
//    ): R {
//        val deferred = CompletableDeferred<R>()
//
//        withContext(Dispatchers.IO) {
//            logger.info {
//                "Starting Playwright browser to resolve video source from $pageUrl"
//            }
//            val playwright = Playwright.create(Playwright.CreateOptions().apply {
//                env = mapOf(
////                    "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD" to "firefox,webkit"
//                )
//            })
//            val browser = playwright.chromium().launch(
//                BrowserType.LaunchOptions()
//                    .setHeadless(true)
//                    .apply {
//                        proxyConfig?.let { setProxy(it.url) }
//                    }
//            )
//            val page = browser.newPage()
//            page.onRequest { request ->
//                val url = request.url()
//                val matched = resourceMatcher(url)
//                if (matched != null) {
//                    logger.info {
//                        "Found video resource via network interception: $url"
//                    }
//                    deferred.complete(matched)
//                }
//            }
//
//            page.navigate(pageUrl)
//
//            deferred.invokeOnCompletion {
//                page.close()
//                browser.close()
//                playwright.close()
//            }
//        }
//
//        return try {
//            deferred.await()
//        } catch (e: Throwable) {
//            if (deferred.isActive) {
//                deferred.cancel()
//            }
//            throw e
//        }
//    }
//}
