package me.him188.ani.app.torrent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.him188.ani.app.torrent.file.TorrentDeferredFileImpl
import me.him188.ani.app.torrent.file.asSeekableInput
import me.him188.ani.app.torrent.file.readBytes
import me.him188.ani.app.torrent.model.Piece
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds


internal class TorrentDeferredFileTest {
    private val sampleText =
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    private val sampleTextByteArray = sampleText.toByteArray()


    @TempDir
    lateinit var tempDir: File

    private val pieces = Piece.buildPieces(ceil(sampleTextByteArray.size.toFloat() / 16).toInt()) { 16 }

    private val tempFile by lazy {
        tempDir.resolve("test.txt").apply { writeText(sampleText) }
    }

    private val file: TorrentDeferredFileImpl by lazy {
        TorrentDeferredFileImpl(
            RandomAccessFile(tempFile.absolutePath, "r").asSeekableInput(),
            pieces,
        )
    }

    @Test
    fun findPiece() {
        assertEquals(0, file.findPiece(0))
        assertEquals(0, file.findPiece(2))
        assertEquals(0, file.findPiece(15))
        assertEquals(1, file.findPiece(16))
        assertEquals(1, file.findPiece(20))
        assertEquals(sampleTextByteArray.size / 16, file.findPiece(sampleTextByteArray.lastIndex.toLong()))
    }

    @Test
    fun readFirstPieceNoSuspend() = runTest {
        pieces.first().state.emit(PieceState.FINISHED)
        file.readBytes().run {
            assertEquals(16, size)
            assertEquals("Lorem Ipsum is s", String(this))
        }
        assertEquals(16L, file.offset)
    }

    @Test
    fun readFirstPieceSuspend() = runTest(timeout = 1.seconds) {
        assertCoroutineSuspends {
            file.readBytes()
        }
    }

    @Test
    fun readFirstPieceSuspendResume() = runTest {
        launch(start = CoroutineStart.UNDISPATCHED) {
            yield()
            pieces.first().state.emit(PieceState.FINISHED)
        }
        file.readBytes().run {
            assertEquals(16, size)
            assertEquals("Lorem Ipsum is s", String(this))
        }
        assertEquals(16L, file.offset)
    }

    @Test
    fun seekFirstNoSuspend() = runTest {
        pieces.first().state.emit(PieceState.FINISHED)
        file.seek(1)
        assertEquals(1L, file.offset)
    }

    @Test
    fun seekFirstSuspend() = runTest {
        launch(start = CoroutineStart.UNDISPATCHED) {
            yield()
            pieces.first().state.emit(PieceState.FINISHED)
        }
        assertCoroutineSuspends {
            file.seek(1)
        }
        assertEquals(1L, file.offset)
    }

    @Test
    fun `seek first complete only when get that piece`() = runTest {
        pieces[2].state.emit(PieceState.FINISHED)
        assertCoroutineSuspends {
            file.seek(1)
        }
        assertEquals(1L, file.offset)
    }

    @Test
    fun seekToSecondPiece() = runTest {
        launch(start = CoroutineStart.UNDISPATCHED) {
            yield()
            pieces[1].state.emit(PieceState.FINISHED)
            println("Piece finished")
        }
        file.seek(17)
        assertEquals(17L, file.offset)
    }

    @Test
    fun seekReadSecondPiece() = runTest {
        pieces[1].state.emit(PieceState.FINISHED)
        file.seek(16)
        assertEquals(16L, file.offset)
        file.readBytes().run {
            assertEquals(16, size)
            assertEquals("imply dummy text", String(this))
        }
    }

    @Test
    fun seekReadSecondPieceMiddle() = runTest {
        pieces[1].state.emit(PieceState.FINISHED)
        file.seek(17)
        assertEquals(17L, file.offset)
        file.readBytes().run {
            assertEquals(15, size)
            assertEquals("mply dummy text", String(this))
        }
    }

    @Test
    fun seekReadLastPiece() = runTest {
        pieces.last().state.emit(PieceState.FINISHED)
        file.seek(pieces.last().offset + 2)
        file.readBytes().run {
            assertEquals(sampleTextByteArray.size % 16 - 2, size)
            assertEquals("Lorem Ipsum.", String(this))
        }
    }
}

private suspend inline fun assertCoroutineSuspends(crossinline block: suspend () -> Unit) {
    var suspended = false
    try {
        coroutineScope {
            val parent = this
            launch(start = CoroutineStart.UNDISPATCHED) {
                yield()
                suspended = true
                parent.cancel()
            }

            launch(start = CoroutineStart.UNDISPATCHED) {
                block()
            }
        }
    } catch (_: CancellationException) {
    }
    if (!suspended) {
        throw AssertionError("Expected coroutine suspends, but it did not suspend")
    }
}

private suspend inline fun assertCoroutineNotSuspend(crossinline block: suspend () -> Unit) {
    coroutineScope {
        val job = launch {
            yield()
            throw AssertionError("Expected coroutine does not suspend, but it did suspend")
        }

        block()
        job.cancel()
    }
}