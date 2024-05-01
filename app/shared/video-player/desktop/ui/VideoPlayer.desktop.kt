package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import javafx.embed.swing.JFXPanel
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.util.Locale
import javax.swing.JPanel
import kotlin.math.roundToInt


@Composable
actual fun VideoPlayer(
    playerState: PlayerState,
    modifier: Modifier,
) {
    val mediaPlayerComponent = remember { initializeMediaPlayerComponent() }
    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer() }
//    mediaPlayer.emitProgressTo(progressState)
//    mediaPlayer.setupVideoFinishHandler { }

    val factory = remember { { mediaPlayerComponent } }
    /* OR the following code and using SwingPanel(factory = { factory }, ...) */
    // val factory by rememberUpdatedState(mediaPlayerComponent)
//
    val url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
    val isFullscreen = false
    LaunchedEffect(url) {
        mediaPlayer.submit {
            mediaPlayer.media().play/*OR .start*/(url)
        }
    }
//    LaunchedEffect(seek) { mediaPlayer.controls().setPosition(seek) }
//    LaunchedEffect(speed) { mediaPlayer.controls().setRate(speed) }
//    LaunchedEffect(volume) { mediaPlayer.audio().setVolume(volume.toPercentage()) }
//    LaunchedEffect(isResumed) { mediaPlayer.controls().setPause(!isResumed) }
    LaunchedEffect(isFullscreen) {
        if (mediaPlayer is EmbeddedMediaPlayer) {
            /*
             * To be able to access window in the commented code below,
             * extend the player composable function from WindowScope.
             * See https://github.com/JetBrains/compose-jb/issues/176#issuecomment-812514936
             * and its subsequent comments.
             *
             * We could also just fullscreen the whole window:
             * `window.placement = WindowPlacement.Fullscreen`
             * See https://github.com/JetBrains/compose-multiplatform/issues/1489
             */
            // mediaPlayer.fullScreen().strategy(ExclusiveModeFullScreenStrategy(window))
            mediaPlayer.fullScreen().toggle()
        }
    }
    DisposableEffect(Unit) { onDispose(mediaPlayer::release) }
    SwingPanel(
        factory = factory,
        background = Color.Transparent,
        modifier = modifier.focusable(false)
    )


//    val view = remember {
//        ImageView()
//    }
//    val mediaPlayer = remember { MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer() }
////    mediaPlayer.emitProgressTo(progressState)
//    mediaPlayer.setupVideoFinishHandler {}
//
//    mediaPlayer.videoSurface().set(ImageViewVideoSurface(view))
//
//    val factory = remember { { view } }
//    /* OR the following code and using SwingPanel(factory = { factory }, ...) */
//    // val factory by rememberUpdatedState(mediaPlayerComponent)
//
////    mediaPlayer.media().play()
//    val url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//    val isFullscreen = false
//    LaunchedEffect(url) { mediaPlayer.media().play/*OR .start*/(url) }
////    LaunchedEffect(seek) { mediaPlayer.controls().setPosition(seek) }
////    LaunchedEffect(speed) { mediaPlayer.controls().setRate(speed) }
////    LaunchedEffect(volume) { mediaPlayer.audio().setVolume(volume.toPercentage()) }
////    LaunchedEffect(isResumed) { mediaPlayer.controls().setPause(!isResumed) }
//    LaunchedEffect(isFullscreen) {
//        if (mediaPlayer is EmbeddedMediaPlayer) {
//            /*
//             * To be able to access window in the commented code below,
//             * extend the player composable function from WindowScope.
//             * See https://github.com/JetBrains/compose-jb/issues/176#issuecomment-812514936
//             * and its subsequent comments.
//             *
//             * We could also just fullscreen the whole window:
//             * `window.placement = WindowPlacement.Fullscreen`
//             * See https://github.com/JetBrains/compose-multiplatform/issues/1489
//             */
//            // mediaPlayer.fullScreen().strategy(ExclusiveModeFullScreenStrategy(window))
//            mediaPlayer.fullScreen().toggle()
//        }
//    }
//    DisposableEffect(Unit) { onDispose(mediaPlayer::release) }
////
////    val container = LocalAppWindow.current.window // ComposeWindow
//
//    SwingPanel(
//        factory = { JFXPanel().apply {
//            scene = javafx.scene.Scene(BoarderPane().apply {
//                center = view
//            })
////            add(view)
//        } },
//        modifier = Modifier,
//    )
////    JavaFXPanel(
////        root = container,
////        panel = JFXPanel().apply {
////            add(view)
////        },
////        onCreate = { view.scene = null }
////    )
}

@Composable
public fun JavaFXPanel(
    root: Container,
    panel: JFXPanel,
    onCreate: () -> Unit
) {
    val container = remember { JPanel() }
    val density = LocalDensity.current.density

    Layout(
        content = {},
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size
            container.setBounds(
                (location.x / density).toInt(),
                (location.y / density).toInt(),
                (size.width / density).toInt(),
                (size.height / density).toInt()
            )
            container.validate()
            container.repaint()
        },
        measurePolicy = { _, _ ->
            layout(0, 0) {}
        }
    )

    DisposableEffect(Unit) {
        container.apply {
            layout = BorderLayout(0, 0)
            add(panel)
        }
        root.add(container)
        onCreate.invoke()
        onDispose {
            root.remove(container)
        }
    }
}

private fun Float.toPercentage(): Int = (this * 100).roundToInt()

/**
 * See https://github.com/caprica/vlcj/issues/887#issuecomment-503288294
 * for why we're using CallbackMediaPlayerComponent for macOS.
 */
private fun initializeMediaPlayerComponent(): Component {
    NativeDiscovery().discover()
    return CallbackMediaPlayerComponent()
//    
//    return if (isMacOS()) {
//        CallbackMediaPlayerComponent()
//    } else {
//        EmbeddedMediaPlayerComponent()
//    }
}

/**
 * We play the video again on finish (so the player is kind of idempotent),
 * unless the [onFinish] callback stops the playback.
 * Using `mediaPlayer.controls().repeat = true` did not work as expected.
 */
@Composable
private fun MediaPlayer.setupVideoFinishHandler(onFinish: (() -> Unit)?) {
    DisposableEffect(onFinish) {
        val listener = object : MediaPlayerEventAdapter() {
            override fun finished(mediaPlayer: MediaPlayer) {
                onFinish?.invoke()
                mediaPlayer.submit { mediaPlayer.controls().play() }
            }
        }
        events().addMediaPlayerEventListener(listener)
        onDispose { events().removeMediaPlayerEventListener(listener) }
    }
}

/**
 * Checks for and emits video progress every 50 milliseconds.
 * Note that it seems vlcj updates the progress only every 250 milliseconds or so.
 *
 * Instead of using `Unit` as the `key1` for [LaunchedEffect],
 * we could use `media().info()?.mrl()` if it's needed to re-launch
 * the effect (for whatever reason) when the url (aka video) changes.
// */
//@Composable
//private fun MediaPlayer.emitProgressTo(state: MutableState<Progress>) {
//    LaunchedEffect(key1 = Unit) {
//        while (isActive) {
//            val fraction = status().position()
//            val time = status().time()
//            state.value = Progress(fraction, time)
//            delay(50)
//        }
//    }
//}

/**
 * Returns [MediaPlayer] from player components.
 * The method names are the same, but they don't share the same parent/interface.
 * That's why we need this method.
 */
private fun Component.mediaPlayer() = when (this) {
    is CallbackMediaPlayerComponent -> mediaPlayer()
    is EmbeddedMediaPlayerComponent -> mediaPlayer()
    else -> error("mediaPlayer() can only be called on vlcj player components")
}

private fun isMacOS(): Boolean {
    val os = System
        .getProperty("os.name", "generic")
        .lowercase(Locale.ENGLISH)
    return "mac" in os || "darwin" in os
}