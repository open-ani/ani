package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoProperties
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.emptyVideoData
import me.him188.ani.app.videoplayer.io.SeekableInputCallbackMedia
import me.him188.ani.app.videoplayer.torrent.HttpStreamingVideoSource
import me.him188.ani.app.videoplayer.ui.VlcjVideoPlayerState.VlcjData
import me.him188.ani.app.videoplayer.ui.state.AbstractPlayerState
import me.him188.ani.app.videoplayer.ui.state.Label
import me.him188.ani.app.videoplayer.ui.state.MutableTrackGroup
import me.him188.ani.app.videoplayer.ui.state.PlaybackState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.SubtitleTrack
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.media.Media
import uk.co.caprica.vlcj.media.MediaEventAdapter
import uk.co.caprica.vlcj.media.MediaParsedStatus
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


@Stable
class VlcjVideoPlayerState(parentCoroutineContext: CoroutineContext) : PlayerState,
    AbstractPlayerState<VlcjData>(parentCoroutineContext) {
    private companion object {
        private val logger = logger<VlcjVideoPlayerState>()

        init {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                try {
                    NativeDiscovery().discover()
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to discover vlcj native libraries" }
                }
            }
        }
    }

    init {
        CallbackMediaPlayerComponent() // init libraries
    }

    val component = run {
        object : ComposeMediaPlayerComponent("-v") { //"-vv", "--avcodec-hw", "none"
//            override fun mouseClicked(e: MouseEvent?) {
//                super.mouseClicked(e)
//                parent.dispatchEvent(e)
//            }
        }
    }
    var bitmap: ImageBitmap by component::composeImage
    val mediaPlayerFactory = MediaPlayerFactory(
        "--video-title=vlcj video output",
        "--no-snapshot-preview",
        "--intf=dummy",
        "-v"
    )
    val player: EmbeddedMediaPlayer = component.mediaPlayer()
//        mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer()

//    val surface = SkiaVideoSurface(
//        renderCallback = {
//            bitmap = it
//        },
//        videoSurfaceAdapter = null
//    ).apply {
//        attach(player)
//    }

//    val surface = mediaPlayerFactory.videoSurfaces().newVideoSurface(
//        object : BufferFormatCallback {
//            override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
//                return BufferFormat(
//                    "RV32",
//                    sourceWidth,
//                    sourceHeight,
//                    intArrayOf(sourceWidth * 4),
//                    intArrayOf(sourceHeight)
//                )
//            }
//
//            override fun allocatedBuffers(buffers: Array<out ByteBuffer>) {
//
//            }
//        },
//        { mediaPlayer, nativeBuffers, bufferFormat ->
//            val buffer = nativeBuffers[0]
//            val out = ByteArray(buffer.capacity())
//            buffer.get(out)
//            val bitmap = Bitmap().apply {
//                this.allocPixels()
//                this.installPixels(out)
//            }
//            this.bitmap = bitmap
//        },
//        false,
//    ).apply {
//        attach(player)
//    }

    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.PAUSED_BUFFERING)

    class VlcjData(
        override val videoSource: VideoSource<*>,
        override val videoData: VideoData,
        val setPlay: () -> Unit,
        releaseResource: () -> Unit
    ) : Data(videoSource, videoData, releaseResource)

    override suspend fun openSource(source: VideoSource<*>): VlcjData {
//        if (source !is FileVideoSource) {
//            throw VideoSourceOpenException(
//                OpenFailures.UNSUPPORTED_VIDEO_SOURCE,
//                IllegalStateException("Unsupported video source: $source")
//            )
//        }
        if (source is HttpStreamingVideoSource) {
            return VlcjData(
                source,
                emptyVideoData(),
                setPlay = {

                    player.media().play(
                        source.uri,
                        *buildList {
                            add("http-user-agent=${source.webVideo.headers["User-Agent"] ?: "Mozilla/5.0"}")
                            val referer = source.webVideo.headers["Referer"]
                            if (referer != null) {
                                add("http-referrer=${referer}")
                            }
                        }.toTypedArray()
                    )
                    lastMedia = null
                },
                releaseResource = {}
            )
        }

        val data = source.open()
        val input = data.createInput()
        return VlcjData(
            source,
            data,
            setPlay = {
                val new = SeekableInputCallbackMedia(input)
                player.media().play(new)
                lastMedia = new
            },
            releaseResource = {
                data.close()
                input.close()
            },
        )
    }

    override fun closeImpl() {
//        component.release()
        lastMedia = null
    }

    private var lastMedia: SeekableInputCallbackMedia? = null // keep referenced so won't be gc'ed

    override suspend fun startPlayer(data: VlcjData) {
        data.setPlay()

//        player.media().options().add(*arrayOf(":avcodec-hw=none")) // dxva2
//        player.controls().play()
//        player.media().play/*OR .start*/(data.videoData.file.absolutePath)
    }

    override suspend fun cleanupPlayer() {
        player.controls().stop()
    }

    override val videoProperties: MutableStateFlow<VideoProperties?> = MutableStateFlow(null)
    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)

    override fun getExactCurrentPositionMillis(): Long = player.status().time()

    override val bufferedPercentage = MutableStateFlow(0)

    override fun pause() {
        player.controls().pause()
    }

    override fun resume() {
        player.controls().play()
    }

    override val playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1.0f)
    override val subtitleTracks: MutableTrackGroup<SubtitleTrack> = MutableTrackGroup()

    init {
        // NOTE: must not call native player in a event
        player.events().addMediaEventListener(object : MediaEventAdapter() {
            override fun mediaParsedChanged(media: Media, newStatus: MediaParsedStatus) {
                if (newStatus == MediaParsedStatus.DONE) {
                    videoProperties.value = createVideoProperties()
                }
            }
        })
        player.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
                videoProperties.value = videoProperties.value?.copy(
                    durationMillis = newLength
                )
            }

//            override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
//                if (newCache != 1f) {
//                    state.value = PlaybackState.PAUSED_BUFFERING
//                } else {
//                    state.value = PlaybackState.READY
//                }
//            }

            override fun playing(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.PLAYING
                player.submit { player.media().parsing().parse() }

                subtitleTracks.candidates.value = player.subpictures().trackDescriptions()
                    .filterNot { it.id() == -1 } // "Disable"
                    .map {
                        SubtitleTrack(
                            openResource.value?.videoData?.filename + "-" + it.id(),
                            it.id().toString(),
                            null,
                            listOf(Label(null, it.description()))
                        )
                    }
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.PAUSED
            }

            override fun stopped(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.FINISHED
            }

            override fun error(mediaPlayer: MediaPlayer) {
                logger.error { "vlcj player error" }
                state.value = PlaybackState.ERROR
            }
        })

        backgroundScope.launch {
            while (true) {
                currentPositionMillis.value = player.status().time()
                delay(0.1.seconds)
            }
        }
        backgroundScope.launch {
            subtitleTracks.current.collect { track ->
                try {
                    if (state.value == PlaybackState.READY) {
                        return@collect
                    }
                    if (track == null) {
                        if (player.subpictures().track() != -1) {
                            player.subpictures().setTrack(-1)
                        }
                        return@collect
                    }
                    val id = track.internalId.toIntOrNull() ?: run {
                        logger.error { "Invalid subtitle track id: ${track.id}" }
                        return@collect
                    }
                    val count = player.subpictures().trackCount()
                    if (id > count) {
                        logger.error { "Invalid subtitle track id: $id, count: $count" }
                        return@collect
                    }
                    logger.info { "All ids: ${player.subpictures().trackDescriptions().map { it.id() }}" }
                    player.subpictures().setTrack(id)
                    logger.info { "Set subtitle track to $id (${track.labels.firstOrNull()})" }
                } catch (e: Throwable) {
                    logger.error(e) { "Exception while setting subtitle track" }
                }
            }
        }
    }

    private fun createVideoProperties(): VideoProperties? {
        val info = player.media().info() ?: return null
        val title = player.titles().titleDescriptions().firstOrNull()
        val video = info.videoTracks().firstOrNull() ?: return null
        val audio = info.audioTracks().firstOrNull() ?: return null
        return VideoProperties(
            title = title?.name(),
            heightPx = video.height(),
            widthPx = video.width(),
            videoBitrate = video.bitRate(),
            audioBitrate = audio.bitRate(),
            frameRate = video.frameRate().toFloat(),
            durationMillis = info.duration(),
            fileLengthBytes = 0,
            fileHash = null,
            filename = title?.name() ?: ""
        )
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.controls().setRate(speed)
        playbackSpeed.value = speed
    }

    override fun seekTo(positionMillis: Long) {
        player.controls().setTime(positionMillis)
    }

}

@Composable
actual fun VideoPlayer(
    playerState: PlayerState,
    modifier: Modifier,
) {
    check(playerState is VlcjVideoPlayerState)

    val mediaPlayer = playerState.player
    val isFullscreen = false
    LaunchedEffect(isFullscreen) {
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
//    DisposableEffect(Unit) { onDispose(mediaPlayer::release) }

    Canvas(modifier) {
        //        val colorType = config.toSkiaColorType()
//        val alphaType = if (hasAlpha) ColorAlphaType.PREMUL else ColorAlphaType.OPAQUE
//        val skiaColorSpace = colorSpace.toSkiaColorSpace()
//        val colorInfo = ColorInfo(colorType, alphaType, skiaColorSpace)
//        val imageInfo = ImageInfo(colorInfo, width, height)
//        val bitmap = Bitmap()
//        bitmap.allocPixels(imageInfo)
//        val map =  SkiaBackedImageBitmap(bitmap)
//        bitmap.readPixels()
        fun calculateImageSizeAndOffsetToFillFrame(
            imageWidth: Int,
            imageHeight: Int,
            frameWidth: Int,
            frameHeight: Int
        ): Pair<IntSize, IntOffset> {
            // 计算图片和画框的宽高比
            val imageAspectRatio = imageWidth.toDouble() / imageHeight.toDouble()

            // 初始化最终的宽度和高度
            val finalWidth: Int = frameWidth
            val finalHeight: Int = (frameWidth / imageAspectRatio).toInt()

            // 计算左上角的偏移量
            val offsetX = 0
            val offsetY = (frameHeight - finalHeight) / 2

            return Pair(IntSize(finalWidth, finalHeight), IntOffset(offsetX, offsetY))
        }

        val bitmap = playerState.bitmap
        val (dstSize, dstOffset) = calculateImageSizeAndOffsetToFillFrame(
            bitmap.width, bitmap.height,
            size.width.toInt(), size.height.toInt()
        )
        drawImage(playerState.bitmap, dstSize = dstSize, dstOffset = dstOffset)
    }

//    SwingPanel(
//        factory = {
//            playerState.component
//        },
//        background = Color.Transparent,
//        modifier = modifier.fillMaxSize()
//    )
//    val surface = playerState.component.videoSurfaceComponent()
//
//    // 转发鼠标事件到 Compose
//    DisposableEffect(surface) {
//        val listener = object : MouseAdapter() {
//            override fun mouseClicked(e: MouseEvent) = dispatchToCompose(e)
//            override fun mousePressed(e: MouseEvent) = dispatchToCompose(e)
//            override fun mouseReleased(e: MouseEvent) = dispatchToCompose(e)
//            override fun mouseEntered(e: MouseEvent) = dispatchToCompose(e)
//            override fun mouseExited(e: MouseEvent) = dispatchToCompose(e)
//            override fun mouseDragged(e: MouseEvent) = dispatchToCompose(e)
//            override fun mouseMoved(e: MouseEvent) = dispatchToCompose(e)
//            override fun mouseWheelMoved(e: MouseWheelEvent) = dispatchToCompose(e)
//
//            fun dispatchToCompose(e: MouseEvent) {
//                playerState.component.parent.dispatchEvent(e)
//            }
//        }
//        surface.addMouseListener(listener)
//        surface.addMouseMotionListener(listener)
//        onDispose {
//            surface.removeMouseListener(listener)
//            surface.removeMouseMotionListener(listener)
//        }
//    }

    // 转发键盘事件到 Compose
//    DisposableEffect(surface) {
//        val listener = object : KeyAdapter() {
//            override fun keyPressed(p0: KeyEvent) = dispatchToCompose(p0)
//            override fun keyReleased(p0: KeyEvent) = dispatchToCompose(p0)
//            override fun keyTyped(p0: KeyEvent) = dispatchToCompose(p0)
//            fun dispatchToCompose(e: KeyEvent) {
//                playerState.component.parent.dispatchEvent(e)
//            }
//        }
//        surface.addKeyListener(listener)
//        onDispose {
//            surface.removeKeyListener(listener)
//        }
//    }
}

private fun isMacOS(): Boolean {
    val os = System
        .getProperty("os.name", "generic")
        .lowercase(Locale.ENGLISH)
    return "mac" in os || "darwin" in os
}
