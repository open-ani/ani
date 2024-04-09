package me.him188.ani.app.ui.subject.episode

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.TorrentVideoSource

@Composable
fun EpisodeVideoDebugInfo(
    videoController: VideoSource<*>,
    modifier: Modifier = Modifier
) {
    if (videoController !is TorrentVideoSource) {
        Text("Not a torrent video source")
        return
    }
//    Column(modifier = modifier) {
//        val session by videoController.session.collectAsStateWithLifecycle(null)
//
//        session?.let { session ->
//            Row {
//                val controller by session.torrentDownloadController.collectAsStateWithLifecycle(null)
//                Text("downloading pieces: ${controller?.getDebugInfo()?.state}")
//            }
//        }
//    }
}
