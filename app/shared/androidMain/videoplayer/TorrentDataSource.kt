@file:OptIn(UnstableApi::class)

package me.him188.ani.app.videoplayer

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.torrent.file.DeferredFile
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

class TorrentDataSourceException(
    override val message: String? = null,
    override val cause: Throwable? = null,
    reason: Int = PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
) : DataSourceException(message, cause, reason)

/**
 * Wrap of a [TorrentDownloadSession] into a [DataSource].
 */
class TorrentDataSource(
    private val session: TorrentDownloadSession,
) : BaseDataSource(true) {
    private companion object {
        @JvmStatic
        private val logger = logger(TorrentDataSource::class)
    }

    private var uri: Uri? = null

    private lateinit var file: DeferredFile
    private var opened = false

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }

        if (file.bytesRemaining <= 0L) {
//            logger.info { "Reading buffer, curr offset=${file.offset}, attempting to read $length: EOF" }
            return C.RESULT_END_OF_INPUT
        }

        return runBlocking {
            file.read(buffer, offset, length)
        }.also {
//            logger.info { "Reading buffer, curr offset=${file.offset}, attempting to read $length, result read $it" }
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

            logger.info { "Starting torrent download" }
            file = runBlocking { session.createDeferredFile() }
        }

        logger.info { "Waiting for totalBytes" }

        val torrentLength = runBlocking { session.totalBytes.first() }

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
        logger.info { "Closing DataSource" }
        uri = null
        if (opened) {
            session.close()
            transferEnded()
        }
    }
}