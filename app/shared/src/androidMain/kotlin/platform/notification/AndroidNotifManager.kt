package me.him188.ani.app.platform.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext


class AndroidNotifManager(
    internal val notificationManager: NotificationManagerCompat,
    private val getContext: () -> Context,
    val activityIntent: () -> Intent,
    private val parentCoroutineContext: CoroutineContext,
) : NotifManager() {
    companion object {
        private val logger = logger<AndroidNotifManager>()
        const val EXTRA_REQUEST_CODE = "requestCode"

        fun handleIntent(requestCode: Int): Boolean {
            return AndroidMediaNotif.handleIntent(requestCode)
        }
    }

    override fun registerNormalChannel(
        id: String,
        name: String,
        importance: NotifImportance,
        description: String?
    ): NormalNotifChannel {
        registerChannel(id, importance, name, description)
        return NormalNotifChannelImpl(id)
    }

    override fun registerOngoingChannel(
        id: String,
        name: String,
        importance: NotifImportance,
        description: String?,
    ): OngoingNotifChannel {
        registerChannel(id, importance, name, description)
        return NotifChannelImpl(id)
    }

    override fun registerMediaOngoingChannel(
        id: String,
        name: String,
        importance: NotifImportance,
        description: String?
    ): MediaNotifChannel<MediaNotif> {
        registerChannel(id, importance, name, description)
        return MediaNotifChannelImpl(id, parentCoroutineContext)
    }

    override fun hasPermission(): Boolean {
        return kotlin.runCatching {
            notificationManager.areNotificationsEnabled()
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                getContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
//            } else {
//                notificationManager.areNotificationsEnabled()
//            }
        }.getOrNull() ?: false
    }

    private fun registerChannel(
        id: String,
        importance: NotifImportance,
        name: String,
        description: String?
    ) {
        kotlin.runCatching {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(id, importance.toAndroidImportance()).apply {
                    setName(name)
                    setDescription(description)
                    setVibrationEnabled(false)
                }.build(),
            )
        }.onFailure {
            logger.error(it) { "Failed to create notification channel" }
        }
    }

    private inner class NotifChannelImpl(
        private val channelId: String,
    ) : OngoingNotifChannel {
        override val notif: Notif by lazy {
            AndroidNotif(this@AndroidNotifManager, getContext, channelId)
        }
    }

    private inner class NormalNotifChannelImpl(
        private val channelId: String,
    ) : NormalNotifChannel {
        override fun newNotif(): Notif {
            return AndroidNotif(this@AndroidNotifManager, getContext, channelId)
        }
    }

    private inner class MediaNotifChannelImpl(
        private val channelId: String,
        private val parentCoroutineContext: CoroutineContext,
    ) : MediaNotifChannel<AndroidMediaNotif>() {
        override fun create(tag: String): AndroidMediaNotif =
            AndroidMediaNotif(this@AndroidNotifManager, getContext, channelId, tag, parentCoroutineContext)
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