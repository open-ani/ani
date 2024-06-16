package me.him188.ani.app.ui.subject.episode.notif

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.notification.VideoNotificationState
import me.him188.ani.app.ui.foundation.LocalImageLoader
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VideoNotifEffect(vm: EpisodeViewModel) {
    val scope = rememberCoroutineScope()
    val imageLoader = LocalImageLoader.current
    val coilContext = LocalPlatformContext.current
    DisposableEffect(vm) {
        val state = VideoNotificationState()
        state.setPlayer(vm.playerState)
        scope.launch {
            val properties = vm.playerState.videoProperties.filterNotNull().first()
            state.setDescription(
                title = vm.subjectPresentation.title,
                text = "${vm.episodePresentation.sort} ${vm.episodePresentation.title}",
                length = properties.durationMillis.milliseconds,
            )
            scope.launch {
                kotlin.runCatching {
                    val request = ImageRequest.Builder(coilContext)
                        .data(vm.subjectPresentation.info.imageCommon)
                        .build()

                    (imageLoader.execute(request) as? SuccessResult)
                        ?.image
                        ?.let {
                            state.setAlbumArt(it)
                        }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }

        onDispose {
            state.release()
            scope.cancel()
        }
    }
}
