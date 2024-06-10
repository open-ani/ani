package me.him188.ani.app.platform.notification

import androidx.compose.ui.graphics.ImageBitmap

abstract class NotifManager { // available via inject
    /**
     * 注册一条可以持续更新的通知渠道, 可用于显示下载进度等
     */
    abstract fun registerOngoingChannel(
        id: String,
        name: String,
        importance: NotifImportance = NotifImportance.LOW,
        description: String? = null
    ): OngoingNotifChannel

    val downloadChannel by lazy {
        registerOngoingChannel(
            id = "download",
            name = "缓存进度",
            description = "通知下载进度, 也可以帮助保持在后台运行",
            importance = NotifImportance.LOW,
        )
    }

    open fun createChannels() {
        downloadChannel
    }
}

enum class NotifImportance {
    NONE,
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

enum class NotifPriority {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

sealed interface NotifChannel

/**
 * 只能发送一条持久通知的通道
 */
interface OngoingNotifChannel : NotifChannel {
    /**
     * 该通道的持久通知. 该通知不会发送, 直到 [Notif.show] 被调用.
     */
    val notif: Notif
}

/**
 * 可以发送多条普通一次性通知的通道
 */
interface NormalNotifChannel : NotifChannel {
    /**
     * 创建一条通知. 该通知不会发送, 直到 [Notif.show] 被调用
     */
    fun newNotif(): Notif
}

interface Notif {
    var priority: NotifPriority
    var silent: Boolean
    var ongoing: Boolean

    var contentTitle: String?
    var contentText: String?

    fun setSmallIcon(uri: String) // required
    fun setSmallIcon(bitmap: ImageBitmap)
    fun setLargeIcon(bitmap: ImageBitmap)

    fun setProgress(max: Int, progress: Int)
    fun setIndeterminateProgress()

    /**
     * 更新信息后, 调用此方法以显示通知
     */
    fun show()

    /**
     * 清除通知
     */
    fun cancel()
}


// For desktop
object NoopNotifManager : NotifManager() {
    private object NoopNotif : Notif {
        override var priority: NotifPriority = NotifPriority.DEFAULT
        override var silent: Boolean = false
        override var ongoing: Boolean = false
        override var contentTitle: String? = ""
        override var contentText: String? = ""

        override fun setSmallIcon(uri: String) {}
        override fun setSmallIcon(bitmap: ImageBitmap) {}
        override fun setLargeIcon(bitmap: ImageBitmap) {}
        override fun setProgress(max: Int, progress: Int) {}
        override fun setIndeterminateProgress() {}
        override fun show() {}
        override fun cancel() {}
    }

    private object NoopOngoingNotifChannel : OngoingNotifChannel {
        override val notif: Notif get() = NoopNotif
    }

    override fun registerOngoingChannel(
        id: String,
        name: String,
        importance: NotifImportance,
        description: String?
    ): OngoingNotifChannel = NoopOngoingNotifChannel
}