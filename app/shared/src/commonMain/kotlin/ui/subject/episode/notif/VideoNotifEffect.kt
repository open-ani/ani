package me.him188.ani.app.ui.subject.episode.notif

import androidx.compose.runtime.Composable
import coil3.annotation.ExperimentalCoilApi
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel

@OptIn(ExperimentalCoilApi::class)
@Composable
fun VideoNotifEffect(vm: EpisodeViewModel) {
    // TODO: VideoNotifEffect 
    //  removed because VideoNotificationState is not available anymore

//    val scope = rememberCoroutineScope()
//    val imageLoader = LocalImageLoader.current
//    val coilContext = LocalPlatformContext.current
//    DisposableEffect(vm) {
//        val state = VideoNotificationState()
//        state.setPlayer(vm.playerState)
//        scope.launch {
//            val properties = vm.playerState.videoProperties.filterNotNull().first()
//            state.setDescription(
//                title = vm.subjectPresentation.title,
//                text = "${vm.episodePresentation.sort} ${vm.episodePresentation.title}",
//                length = properties.durationMillis.milliseconds,
//            )
//            scope.launch {
//                kotlin.runCatching {
//                    val request = ImageRequest.Builder(coilContext)
//                        .data(vm.subjectPresentation.info.imageLarge)
//                        .build()
//
//                    (imageLoader.execute(request) as? SuccessResult)
//                        ?.image
//                        ?.let {
//                            state.setAlbumArt(it)
//                        }
//                }.onFailure {
//                    it.printStackTrace()
//                }
//            }
//        }
//
//        onDispose {
//            state.release()
//            scope.cancel()
//        }
//    }
}
