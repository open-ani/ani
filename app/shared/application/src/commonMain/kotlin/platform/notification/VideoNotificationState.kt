package me.him188.ani.app.platform.notification

import androidx.compose.runtime.Stable
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration

@Stable
class VideoNotificationState(
    private val tag: String = "ui/subject/episode/video",
) : KoinComponent {
    private val notificationManager: NotifManager by inject()

    @OptIn(ExperimentalCoilApi::class)
    fun setAlbumArt(albumArt: Image) {
        val notif = mediaNotif()
        notif.updateAlbumArt(albumArt)
    }

    fun setDescription(title: String, text: String, length: Duration) {
        mediaNotif().apply {
            contentTitle = title
            contentText = text
            updateMediaMetadata(album = title, duration = length)
            show()
        }
    }

    fun setPlayer(playerState: PlayerState) {
        val notif = mediaNotif()
        notif.attachPlayerState(playerState)
    }

    private fun mediaNotif() = notificationManager.playChannel.getOrStart(tag)

    fun release() {
        notificationManager.playChannel.releaseCurrent()
    }
}
