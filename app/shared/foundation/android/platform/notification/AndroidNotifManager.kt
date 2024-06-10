package me.him188.ani.app.platform.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import me.him188.ani.R
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.util.concurrent.atomic.AtomicInteger


class AndroidNotifManager(
    internal val notificationManager: NotificationManagerCompat,
    private val getContext: () -> Context,
) : NotifManager() {
    private companion object {
        private val logger = logger<AndroidNotifManager>()
    }

    override fun registerOngoingChannel(
        id: String,
        name: String,
        importance: NotifImportance,
        description: String?,
    ): OngoingNotifChannel {
        kotlin.runCatching {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(id, importance.toAndroidImportance()).apply {
                    setName(name)
                    setDescription(description)
                    setVibrationEnabled(false)
                }.build()
            )
        }.onFailure {
            logger.error(it) { "Failed to create notification channel" }
        }
        return NotifChannelImpl(id)
    }

    private inner class NotifChannelImpl(
        private val channelId: String,
    ) : OngoingNotifChannel {
        override val notif: Notif by lazy {
            AndroidNotif(this@AndroidNotifManager, getContext, channelId)
        }
    }
}

private class AndroidNotif(
    private val manager: AndroidNotifManager,
    private val getContext: () -> Context,
    private val channelId: String,
) : Notif {
    companion object {
        private val globalId = AtomicInteger(0)
        private val logger = logger<AndroidNotif>()
        fun nextNotificationId(): Int = globalId.incrementAndGet()
    }

    private var builder = NotificationCompat.Builder(getContext(), channelId).apply {
        setSmallIcon(R.mipmap.a_round) // 必须要有一个 icon
    }

    private val id = nextNotificationId()

    override var priority: NotifPriority = NotifPriority.DEFAULT
        set(value) {
            builder.setPriority(value.toAndroidPriority())
            field = value
        }
    override var silent: Boolean = false
        set(value) {
            builder.setSilent(value)
            field = value
        }
    override var ongoing: Boolean = false
        set(value) {
            builder.setOngoing(value)
            field = value
        }
    override var contentTitle: String? = ""
        set(value) {
            builder.setContentTitle(value)
            field = value
        }
    override var contentText: String? = ""
        set(value) {
            builder.setContentText(value)
            field = value
        }

    override fun setSmallIcon(uri: String) {
        kotlin.runCatching {
            builder.setSmallIcon(IconCompat.createWithContentUri(uri))
        }.onFailure {
            logger.error(it) { "Failed to set small icon" }
        }
    }

    override fun setSmallIcon(bitmap: ImageBitmap) {
        kotlin.runCatching {
            builder.setSmallIcon(IconCompat.createWithAdaptiveBitmap(bitmap.asAndroidBitmap()))
        }.onFailure {
            logger.error(it) { "Failed to set small icon" }
        }
    }

    override fun setLargeIcon(bitmap: ImageBitmap) {
        kotlin.runCatching {
            builder.setLargeIcon(bitmap.asAndroidBitmap())
        }.onFailure {
            logger.error(it) { "Failed to set large icon" }
        }
    }

    override fun setProgress(max: Int, progress: Int) {
        builder.setProgress(max, progress, false)
    }

    override fun setIndeterminateProgress() {
        builder.setProgress(0, 0, true)
    }

    @SuppressLint("MissingPermission")
    override fun show() {
        kotlin.runCatching {
            if (ContextCompat.checkSelfPermission(
                    getContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                logger.warn { "No notification permission, ignoring" }
            }

            manager.notificationManager.notify(id, builder.build())
        }.onFailure {
            logger.error(it) { "Failed to show notification" }
        }
    }

    override fun cancel() {
        kotlin.runCatching {
            manager.notificationManager.cancel(id)
        }.onFailure {
            logger.error(it) { "Failed to cancel notification" }
        }
    }

    override fun toString(): String {
        return "AndroidNotif(id=$id, channelId=$channelId)"
    }
}

private fun NotifPriority.toAndroidPriority(): Int {
    return when (this) {
        NotifPriority.MIN -> NotificationCompat.PRIORITY_MIN
        NotifPriority.LOW -> NotificationCompat.PRIORITY_LOW
        NotifPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
        NotifPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
        NotifPriority.MAX -> NotificationCompat.PRIORITY_MAX
    }
}


private fun NotifImportance.toAndroidImportance(): Int {
    return when (this) {
        NotifImportance.NONE -> NotificationManager.IMPORTANCE_NONE
        NotifImportance.MIN -> NotificationManager.IMPORTANCE_MIN
        NotifImportance.LOW -> NotificationManager.IMPORTANCE_LOW
        NotifImportance.DEFAULT -> NotificationManager.IMPORTANCE_DEFAULT
        NotifImportance.HIGH -> NotificationManager.IMPORTANCE_HIGH
        NotifImportance.MAX -> NotificationManager.IMPORTANCE_MAX
    }
}
