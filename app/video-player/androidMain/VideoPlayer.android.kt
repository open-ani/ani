package me.him188.ani.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = ExoPlayer.Builder(context).apply {

                }.build().apply {
                    prepare()
                }
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier,
        onRelease = { it.player?.release() }
    ) {

    }
}

@Composable
@Preview(widthDp = 160 * 4, heightDp = 90 * 4, showBackground = true)
internal actual fun PreviewVideoPlayer() {
    VideoPlayer(Modifier.fillMaxSize())
}