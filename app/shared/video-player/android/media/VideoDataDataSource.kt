@file:androidx.annotation.OptIn(UnstableApi::class)

package me.him188.ani.app.videoplayer.media

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

/**
 * Wrap of an Ani [VideoData] into a ExoPlayer [DataSource].
 *
 * This class will not close [videoData].
 */
@androidx.annotation.OptIn(UnstableApi::class)
class VideoDataDataSource(
    private val videoData: VideoData,
) : BaseDataSource(true) {
    private companion object {
        @JvmStatic
        private val logger = logger(VideoDataDataSource::class)
    }

    private var uri: Uri? = null

    private lateinit var file: SeekableInput
    private var opened = false

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }

        if (file.bytesRemaining <= 0L) {
            return C.RESULT_END_OF_INPUT
        }

        return runBlocking {
            file.read(buffer, offset, length)
        }.also {
            bytesTransferred(it)
        }
    }

    override fun open(dataSpec: DataSpec): Long {
        logger.info { "Opening dataSpec, offset=${dataSpec.position}, length=${dataSpec.length}" }

        val uri = dataSpec.uri
        if (opened && dataSpec.uri == this.uri) {
            logger.info { "Double open, will not start download." }
        } else {
            this.uri = uri
            transferInitializing(dataSpec)

            logger.info { "Acquiring torrent DeferredFile" }
            file = runBlocking { videoData.createInput() }
            opened = true
        }

        logger.info { "Waiting for totalBytes" }

        val torrentLength = runBlocking { videoData.fileLength }

        logger.info { "torrentLength = $torrentLength" }

        if (dataSpec.position >= torrentLength) {
            logger.info { "dataSpec.position ${dataSpec.position} > torrentLength $torrentLength" }
        } else {
            if (dataSpec.position != -1L && dataSpec.position != 0L) {
                logger.info { "Seeking to ${dataSpec.position}" }
                runBlocking { file.seek(dataSpec.position) }
            }

            logger.info { "Open done, bytesRemaining = ${file.bytesRemaining}" }
        }

        transferStarted(dataSpec)
        return file.bytesRemaining
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        logger.info { "Closing VideoDataDataSource" }
        uri = null
        if (opened) {
            file.close()
            transferEnded()
        }
    }
}