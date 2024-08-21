package me.him188.ani.app.videoplayer

import android.net.Uri
import android.util.Pair
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.annotation.UiThread
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.trackselection.TrackSelection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoProperties
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.emptyVideoData
import me.him188.ani.app.videoplayer.media.VideoDataDataSource
import me.him188.ani.app.videoplayer.torrent.HttpStreamingVideoSource
import me.him188.ani.app.videoplayer.ui.state.AbstractPlayerState
import me.him188.ani.app.videoplayer.ui.state.AudioTrack
import me.him188.ani.app.videoplayer.ui.state.Chapter
import me.him188.ani.app.videoplayer.ui.state.Label
import me.him188.ani.app.videoplayer.ui.state.MutableTrackGroup
import me.him188.ani.app.videoplayer.ui.state.PlaybackState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.app.videoplayer.ui.state.SubtitleTrack
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.interfaces.IMedia.Meta
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


class ExoPlayerStateFactory : PlayerStateFactory {
    @OptIn(UnstableApi::class)
    override fun create(context: Context, parentCoroutineContext: CoroutineContext): PlayerState =
        ExoPlayerState(context, parentCoroutineContext)
}


@OptIn(UnstableApi::class)
internal class ExoPlayerState @UiThread constructor(
    context: Context,
    parentCoroutineContext: CoroutineContext
) : AbstractPlayerState<ExoPlayerState.ExoPlayerData>(parentCoroutineContext),
    AutoCloseable {
    class ExoPlayerData(
        videoSource: VideoSource<*>,
        videoData: VideoData,
        releaseResource: () -> Unit,
        val setMedia: () -> Unit,
    ) : Data(videoSource, videoData, releaseResource)

    override suspend fun startPlayer(data: ExoPlayerData) {
        withContext(Dispatchers.Main.immediate) {
            data.setMedia()
            player.prepare()
            player.play()
        }
    }

    override suspend fun cleanupPlayer() {
        withContext(Dispatchers.Main.immediate) {
            player.stop()
            player.clearMediaItems()
        }
    }

    override suspend fun openSource(source: VideoSource<*>): ExoPlayerData {
        if (source is HttpStreamingVideoSource) {
            return ExoPlayerData(
                source,
                emptyVideoData(),
                releaseResource = {},
                setMedia = {
                    val headers = source.webVideo.headers
                    val item = MediaItem.Builder().apply {
                        setUri(source.uri)
                        setSubtitleConfigurations(
                            source.extraFiles.subtitles.map {
                                MediaItem.SubtitleConfiguration.Builder(
                                    Uri.parse(it.uri),
                                ).apply {
                                    it.mimeType?.let { mimeType -> setMimeType(mimeType) }
                                    it.language?.let { language -> setLanguage(language) }
                                }.build()
                            },
                        )
                    }.build()
                    player.setMediaSource(
                        DefaultMediaSourceFactory(
                            DefaultHttpDataSource.Factory()
                                .setUserAgent(
                                    headers["User-Agent"]
                                        ?: """Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3""",
                                )
                                .setDefaultRequestProperties(headers)
                                .setConnectTimeoutMs(30_000),
                        ).createMediaSource(item),
                    )
                },
            )
        }
        val data = source.open()
        val file = withContext(Dispatchers.IO) {
            data.createInput()
        }
        val factory = ProgressiveMediaSource.Factory {
            VideoDataDataSource(data, file)
        }
        return ExoPlayerData(
            source,
            data,
            releaseResource = {
                file.close()
                backgroundScope.launch(NonCancellable) {
                    data.close()
                }
            },
            setMedia = {
                player.setMediaSource(factory.createMediaSource(MediaItem.fromUri(source.uri)))
            },
        )
    }

    private val updateVideoPropertiesTasker = MonoTasker(backgroundScope)

    val player = kotlin.run {
        ExoPlayer.Builder(context).apply {
            setTrackSelector(
                object : DefaultTrackSelector(context) {
                    override fun selectTextTrack(
                        mappedTrackInfo: MappedTrackInfo,
                        rendererFormatSupports: Array<out Array<IntArray>>,
                        params: Parameters,
                        selectedAudioLanguage: String?
                    ): Pair<ExoTrackSelection.Definition, Int>? {
                        val preferred = subtitleTracks.current.value
                            ?: return super.selectTextTrack(
                                mappedTrackInfo,
                                rendererFormatSupports,
                                params,
                                selectedAudioLanguage,
                            )

                        infix fun SubtitleTrack.matches(group: TrackGroup): Boolean {
                            if (this.internalId == group.id) return true

                            if (this.labels.isEmpty()) return false
                            for (index in 0 until group.length) {
                                val format = group.getFormat(index)
                                if (format.labels.isEmpty()) {
                                    continue
                                }
                                if (this.labels.any { it.value == format.labels.first().value }) {
                                    return true
                                }
                            }
                            return false
                        }

                        // 备注: 这个实现可能并不好, 他只是恰好能跑
                        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                            if (C.TRACK_TYPE_TEXT != mappedTrackInfo.getRendererType(rendererIndex)) continue

                            val groups = mappedTrackInfo.getTrackGroups(rendererIndex)
                            for (groupIndex in 0 until groups.length) {
                                val trackGroup = groups[groupIndex]
                                if (preferred matches trackGroup) {
                                    return Pair(
                                        ExoTrackSelection.Definition(
                                            trackGroup,
                                            IntArray(trackGroup.length) { it }, // 如果选择所有字幕会闪烁
                                            TrackSelection.TYPE_UNSET,
                                        ),
                                        rendererIndex,
                                    )
                                }
                            }
                        }
                        return super.selectTextTrack(
                            mappedTrackInfo,
                            rendererFormatSupports,
                            params,
                            selectedAudioLanguage,
                        )
                    }
                },
            )
        }.build().apply {
            playWhenReady = true
            addListener(
                object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        subtitleTracks.candidates.value =
                            tracks.groups.asSequence()
                                .filter { it.type == C.TRACK_TYPE_TEXT }
                                .flatMapIndexed { groupIndex: Int, group: Tracks.Group ->
                                    group.getSubtitleTracks()
                                }
                                .toList()
                        audioTracks.candidates.value =
                            tracks.groups.asSequence()
                                .filter { it.type == C.TRACK_TYPE_AUDIO }
                                .flatMapIndexed { groupIndex: Int, group: Tracks.Group ->
                                    group.getAudioTracks()
                                }
                                .toList()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        state.value = PlaybackState.ERROR
                        logger.warn("ExoPlayer error: ${error.errorCodeName}", error)
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                        updateVideoProperties()
                    }

                    @MainThread
                    private fun updateVideoProperties(): Boolean {
                        val video = videoFormat ?: return false
                        val audio = audioFormat ?: return false
                        val data = openResource.value?.videoData ?: return false
                        val title = mediaMetadata.title
                        val duration = duration

                        // 注意, 要把所有 UI 属性全都读出来然后 captured 到 background -- ExoPlayer 所有属性都需要在主线程

                        updateVideoPropertiesTasker.launch(Dispatchers.IO) {
                            // This is in background
                            videoProperties.value = VideoProperties(
                                title = title?.toString(),
                                heightPx = video.height,
                                widthPx = video.width,
                                videoBitrate = video.bitrate,
                                audioBitrate = audio.bitrate,
                                frameRate = video.frameRate,
                                durationMillis = duration,
                                fileLengthBytes = data.fileLength,
                                fileHash = data.computeHash(),
                                filename = data.filename,
                            )
                        }
                        return true
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                state.value = PlaybackState.PAUSED_BUFFERING
                                isBuffering.value = true
                            }

                            Player.STATE_ENDED -> {
                                state.value = PlaybackState.FINISHED
                                isBuffering.value = false
                            }

                            Player.STATE_IDLE -> {
                                state.value = PlaybackState.READY
                                isBuffering.value = false
                            }

                            Player.STATE_READY -> {
                                state.value = PlaybackState.READY
                                isBuffering.value = false
                            }
                        }
                        updateVideoProperties()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            state.value = PlaybackState.PLAYING
                            isBuffering.value = false
                        } else {
                            state.value = PlaybackState.PAUSED
                        }
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        super.onPlaybackParametersChanged(playbackParameters)
                        playbackSpeed.value = playbackParameters.speed
                    }
                },
            )
        }
    }

    private fun Tracks.Group.getSubtitleTracks() = sequence {
        repeat(length) { index ->
            val format = getTrackFormat(index)
            val firstLabel = format.labels.firstNotNullOfOrNull { it.value }
            format.metadata
            this.yield(
                SubtitleTrack(
                    "${openResource.value?.videoData?.filename}-${mediaTrackGroup.id}-$index",
                    mediaTrackGroup.id,
                    firstLabel ?: mediaTrackGroup.id,
                    format.labels.map { Label(it.language, it.value) },
                ),
            )
        }
    }

    private fun Tracks.Group.getAudioTracks() = sequence {
        repeat(length) { index ->
            val format = getTrackFormat(index)
            val firstLabel = format.labels.firstNotNullOfOrNull { it.value }
            format.metadata
            this.yield(
                AudioTrack(
                    "${openResource.value?.videoData?.filename}-${mediaTrackGroup.id}-$index",
                    mediaTrackGroup.id,
                    firstLabel ?: mediaTrackGroup.id,
                    format.labels.map { Label(it.language, it.value) },
                ),
            )
        }
    }

    override val isBuffering: MutableStateFlow<Boolean> = MutableStateFlow(false) // 需要单独状态, 因为要用户可能会覆盖 [state] 
    override fun stopImpl() {
        player.stop()
    }

    override val videoProperties = MutableStateFlow<VideoProperties?>(null)
    override val bufferedPercentage = MutableStateFlow(0)

    override fun seekTo(positionMillis: Long) {
        player.seekTo(positionMillis)
    }

    override val subtitleTracks: MutableTrackGroup<SubtitleTrack> = MutableTrackGroup()

    override val audioTracks: MutableTrackGroup<AudioTrack> = MutableTrackGroup()
    override fun saveScreenshotFile(filename: String) {
        TODO("Not yet implemented")
    }

    override val chapters: StateFlow<ImmutableList<Chapter>> = MutableStateFlow(persistentListOf())

    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)
    override fun getExactCurrentPositionMillis(): Long = player.currentPosition

    init {
        backgroundScope.launch(Dispatchers.Main) {
            while (currentCoroutineContext().isActive) {
                currentPositionMillis.value = player.currentPosition
                bufferedPercentage.value = player.bufferedPercentage
                delay(0.1.seconds) // 10 fps
            }
        }
        backgroundScope.launch(Dispatchers.Main) {
            subtitleTracks.current.collect {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().apply {
                    setPreferredTextLanguage(it?.internalId) // dummy value to trigger a select, we have custom selector
                }.build()
            }
        }
    }

    override fun pause() {
        player.playWhenReady = false
        player.pause()
    }

    override fun resume() {
        player.playWhenReady = true
        player.play()
    }

    override val playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1f)

    override fun closeImpl() {
        @kotlin.OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                player.stop()
                player.release()
                logger.info("ExoPlayer $player released")
            } catch (e: Throwable) {
                logger.error(e) { "Failed to release ExoPlayer $player, ignoring" }
            }
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }
}

class LibVlcAndroidPlayerFactory : PlayerStateFactory {
    @OptIn(UnstableApi::class)
    override fun create(context: Context, parentCoroutineContext: CoroutineContext): PlayerState =
        LibVlcAndroidPlayerState(context, parentCoroutineContext)

}

@OptIn(UnstableApi::class)
internal class LibVlcAndroidPlayerState @UiThread constructor(
    context: Context,
    parentCoroutineContext: CoroutineContext
) : AbstractPlayerState<LibVlcAndroidPlayerState.LibVlcAndroidPlayerData>(parentCoroutineContext),
    AutoCloseable {

    class LibVlcAndroidPlayerData(
        videoSource: VideoSource<*>,
        videoData: VideoData,
        releaseResource: () -> Unit,
        val setMedia: () -> Unit,
    ) : Data(videoSource, videoData, releaseResource)

    private fun reloadAudioTracks() {
        if (player.audioTrack < 0 || player.audioTracks == null) return
        audioTracks.candidates.value =
            player.audioTracks.asSequence()
                .filterNot { it.id == -1 } // "Disable"
                .mapNotNull { AudioTrack(it.id.toString(), it.id.toString(), it.name, mutableListOf()) }
                .toList()
    }

    private fun reloadSubtitleTracks() {
        if (player.spuTracksCount < 0 || player.spuTracks == null) return
        subtitleTracks.candidates.value =
            player.spuTracks.asSequence()
                .filterNot { it.id == -1 } // "Disable"
                .mapNotNull { SubtitleTrack(it.id.toString(), it.id.toString(), it.name, mutableListOf()) }
                .toList()
    }

    private fun reloadChapters() {
        TODO("Impl Reload Chapters")
    }

    private fun reloadVideoProperties() {
        if (!player.isSeekable) return
        val currentVideoTrack: IMedia.VideoTrack? = player.currentVideoTrack
        val currentMediaTrack: IMedia? = player.media
        val title = currentMediaTrack?.getMeta(Meta.Title) ?: ""
        val duration = player.media?.duration ?: 0
        videoProperties.value = VideoProperties(
            title = title,
            heightPx = currentVideoTrack?.height ?: 0,
            widthPx = currentVideoTrack?.width ?: 0,
            videoBitrate = currentVideoTrack?.bitrate ?: 0,
            audioBitrate = 0,
            frameRate = currentVideoTrack?.frameRateNum?.toFloat() ?: 0f,
            durationMillis = duration,
            fileLengthBytes = player.length,
            fileHash = "",
            filename = title,
        )
    }


    private val listener: MediaPlayer.EventListener = MediaPlayer.EventListener { event ->
        when (event.type) {
            MediaPlayer.Event.Buffering -> {
                if (player.isPlaying) {
                    state.value = PlaybackState.PLAYING
                    isBuffering.value = false
                } else {
                    state.value = PlaybackState.PAUSED_BUFFERING
                }
            }

            MediaPlayer.Event.Playing -> {
                state.value = PlaybackState.PLAYING
                reloadVideoProperties()
                reloadAudioTracks()
                reloadSubtitleTracks()
            }

            MediaPlayer.Event.Paused -> {
                state.value = PlaybackState.PAUSED
            }

            MediaPlayer.Event.ESAdded -> {
                reloadAudioTracks()
                reloadSubtitleTracks()
            }

        }
        
    }

    private val libVlc = LibVLC(context, mutableListOf("-vvv"))
    val player = kotlin.run {
        MediaPlayer(libVlc)
            .apply {
                setEventListener(listener)
            }
    }

    override val videoProperties = MutableStateFlow<VideoProperties?>(null)
    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)
    override val bufferedPercentage = MutableStateFlow(0)
    override val playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1f)
    override val subtitleTracks: MutableTrackGroup<SubtitleTrack> = MutableTrackGroup()
    override val audioTracks: MutableTrackGroup<AudioTrack> = MutableTrackGroup()
    override val chapters: StateFlow<ImmutableList<Chapter>> = MutableStateFlow(persistentListOf())
    override val isBuffering: MutableStateFlow<Boolean> = MutableStateFlow(false) // 需要单独状态, 因为要用户可能会覆盖 [state] 

    override fun getExactCurrentPositionMillis(): Long = player.position.toLong()

    init {
        backgroundScope.launch(Dispatchers.Main) {
            while (currentCoroutineContext().isActive) {
                currentPositionMillis.value = player.time
                delay(0.1.seconds) // 10 fps
            }
        }
        backgroundScope.launch {
            subtitleTracks.current.collect { track ->
                try {
                    if (state.value == PlaybackState.READY) {
                        return@collect
                    }
                    if (track == null) {
                        if (player.spuTrack != -1) {
                            player.spuTrack = -1
                        }
                        return@collect
                    }
                    val id = track.internalId.toIntOrNull() ?: run {
                        logger.error { "Invalid subtitle track id: ${track.id}" }
                        return@collect
                    }
                    if (player.spuTracksCount <= 0 || player.spuTracks == null) {
                        logger.error { "Not exists spu track id: ${track.id}" }
                        return@collect
                    }
                    val subTrackIds = player.spuTracks.map { it.id }
                    logger.info { "All ids: $subTrackIds" }
                    if (!subTrackIds.contains(id)) {
                        logger.error { "Invalid subtitle track id: $id" }
                        return@collect
                    }
                    player.setSpuTrack(id)
                    logger.info { "Set subtitle track to $id (${track.labels.firstOrNull()})" }
                } catch (e: Throwable) {
                    logger.error(e) { "Exception while setting subtitle track" }
                }
            }
        }

        backgroundScope.launch {
            audioTracks.current.collect { track ->
                try {
                    if (state.value == PlaybackState.READY) {
                        return@collect
                    }
                    if (track == null) {
                        if (player.audioTrack != -1) {
                            player.audioTrack = -1
                        }
                        return@collect
                    }

                    val id = track.internalId.toIntOrNull() ?: run {
                        logger.error { "Invalid audio track id: ${track.id}" }
                        return@collect
                    }
                    if (player.audioTracksCount <= 0 || player.audioTracks == null) {
                        logger.error { "Not exists audio track id: ${track.id}" }
                        return@collect
                    }
                    val audioTrackIds = player.audioTracks.map { it.id }
                    if (!audioTrackIds.contains(id)) {
                        logger.error { "Invalid audio track id: $id" }
                        return@collect
                    }
                    logger.info { "All ids: $audioTrackIds" }
                    player.setAudioTrack(id)
                    logger.info { "Set audio track to $id (${track.labels.firstOrNull()})" }
                } catch (e: Throwable) {
                    logger.error(e) { "Exception while setting audio track" }
                }
            }
        }

        backgroundScope.launch {
            openResource.filterNotNull().map { it.videoSource.extraFiles.subtitles }
                .distinctUntilChanged()
                .debounce(1000)
                .collectLatest { urls ->
                    logger.info { "Video ExtraFiles changed, updating slaves" }
                    player.media?.clearSlaves()
                    for (subtitle in urls) {
                        logger.info { "Adding SUBTITLE slave: $subtitle" }
                        player.addSlave(IMedia.Slave.Type.Subtitle, Uri.parse(subtitle.uri), false)
                    }
                }
        }
    }

    override fun stopImpl() {
        player.stop()
    }

    override suspend fun cleanupPlayer() {
        withContext(Dispatchers.Main.immediate) {
            player.stop()
        }
    }

    override suspend fun openSource(source: VideoSource<*>): LibVlcAndroidPlayerData {
        if (source is HttpStreamingVideoSource) {
            return LibVlcAndroidPlayerData(
                source,
                emptyVideoData(),
                releaseResource = {},
                setMedia = {
                    val media = Media(libVlc, Uri.parse(source.uri));
                    player.setMedia(media)
                    media.release();
                },
            )
        }
        val data = source.open()
        val file = withContext(Dispatchers.IO) {
            data.createInput()
        }
        return LibVlcAndroidPlayerData(
            source,
            data,
            releaseResource = {
                file.close()
                backgroundScope.launch(NonCancellable) {
                    data.close()
                }
            },
            setMedia = {
                player.setMedia(Media(libVlc, source.uri))
            },
        )
    }

    override fun closeImpl() {
        @kotlin.OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                player.stop()
                player.detachViews();
                player.release()
                logger.info("LibVlcAndroidPlayer $player released")
            } catch (e: Throwable) {
                logger.error(e) { "Failed to release LibVlcAndroidPlayer $player, ignoring" }
            }
        }
    }

    override suspend fun startPlayer(data: LibVlcAndroidPlayerData) {
        withContext(Dispatchers.Main.immediate) {
            data.setMedia()
            player.play()
        }
    }


    override fun pause() {
        player.pause()
    }

    override fun resume() {
        player.play()
    }


    override fun setPlaybackSpeed(speed: Float) {
        player.rate = speed
    }

    override fun seekTo(positionMillis: Long) {
        if (!player.isSeekable) return
        player.time = positionMillis
    }


    override fun saveScreenshotFile(filename: String) {
        TODO("Not yet implemented")
    }


}