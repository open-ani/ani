package me.him188.ani.app.app

import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

typealias AppCallback = () -> Unit

/** 监听应用事件 */
object AppLifeCycle {
    private val logger = logger(AppLifeCycle::class)
    private val pausedListeners = mutableMapOf<String, AppCallback>()

    /** 添加切换到后台的回调 */
    fun addPausedListener(key: String, callback: AppCallback) {
        logger.info { "add paused listener with key: $key" }
        pausedListeners[key] = callback
    }

    fun callPaused() {
        logger.info { "call paused listeners" }
        for ((key, listener) in pausedListeners) {
            logger.info { "call paused listener with key: $key" }
            listener.invoke()
        }
    }

    private val destroyListeners = mutableMapOf<String, AppCallback>()

    /** 添加退出应用的回调 */
    fun addDestroyListener(key: String, callback: AppCallback) {
        logger.info { "add destroy listener with key: $key" }
        destroyListeners[key] = callback
    }

    fun callDestroy() {
        logger.info { "call destroy listeners" }
        for ((key, listener) in destroyListeners) {
            logger.info { "call destroy listener with key: $key" }
            listener.invoke()
        }
    }
}