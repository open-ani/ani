package me.him188.ani.app.torrent

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.api.PieceState
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.file.TorrentInput
import me.him188.ani.utils.io.asSeekableInput
import me.him188.ani.utils.io.readBytes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals

private const val sampleText =
    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
private val sampleTextByteArray = sampleText.toByteArray()


internal sealed class TorrentInputTest {
    class NoShift : TorrentInputTest() {
        override val logicalPieces = Piece.buildPieces(ceil(sampleTextByteArray.size.toFloat() / 16).toInt()) { 16 }

        @Test
        fun seekReadLastPiece() = runTest {
            logicalPieces.last().state.emit(PieceState.FINISHED)
            file.seek(logicalPieces.last().offset + 2)
            file.readBytes().run {
                assertEquals(sampleTextByteArray.size % 16 - 2, size)
                assertEquals("Lorem Ipsum.", String(this))
            }
        }
    }

    class WithShift : TorrentInputTest() {
        override val logicalPieces = Piece.buildPieces(
            ceil(sampleTextByteArray.size.toFloat() / 16).toInt(),
            initial = 1000
        ) { 16 }

        @Test
        fun seekReadLastPiece() = runTest {
            logicalPieces.last().state.emit(PieceState.FINISHED)
            file.seek(logicalPieces.last().offset - 1000 + 2)
            file.readBytes().run {
                assertEquals(sampleTextByteArray.size % 16 - 2, size)
                assertEquals("Lorem Ipsum.", String(this))
            }
        }
    }

    @TempDir
    lateinit var tempDir: File

    protected abstract val logicalPieces: List<Piece>

    private val tempFile by lazy {
        tempDir.resolve("test.txt").apply {
            parentFile.mkdirs()
            writeText(sampleText)
        }
    }

    protected val file: TorrentInput by lazy {
        TorrentInput(
            tempFile.asSeekableInput(),
            logicalPieces,
        )
    }

    @AfterEach
    fun afterTest() {
        file.close()
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
        logicalPieces.first().state.emit(PieceState.FINISHED)
        file.readBytes().run {
            assertEquals(16, size)
            assertEquals("Lorem Ipsum is s", String(this))
        }
        assertEquals(16L, file.offset)
    }

    // TODO: TorrentInputTest has been disabled because we changed `seek` from suspend to blocking to improve speed

//    @Test
//    fun readFirstPieceSuspendResume() = runTest {
//        launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//            logicalPieces.first().state.emit(PieceState.FINISHED)
//        }
//        file.readBytes().run {
//            assertEquals(16, size)
//            assertEquals("Lorem Ipsum is s", String(this))
//        }
//        assertEquals(16L, file.offset)
//    }

    @Test
    fun seekFirstNoSuspend() = runTest {
        logicalPieces.first().state.emit(PieceState.FINISHED)
        file.seek(1)
        assertEquals(1L, file.offset)
    }

//    @Test
//    fun seekFirstSuspend() = runTest {
//        launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//            logicalPieces.first().state.emit(PieceState.FINISHED)
//        }
//        assertCoroutineSuspends {
//            file.seek(1)
//        }
//        assertEquals(1L, file.offset)
//    }

//    @Test
//    fun `seek first complete only when get that piece`() = runTest {
//        logicalPieces[2].state.emit(PieceState.FINISHED)
//        assertCoroutineSuspends {
//            file.seek(1)
//        }
//        assertEquals(1L, file.offset)
//    }

//    @Test
//    fun seekToSecondPiece() = runTest {
//        launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//            logicalPieces[1].state.emit(PieceState.FINISHED)
//            println("Piece finished")
//        }
//        file.seek(17)
//        assertEquals(17L, file.offset)
//    }

    @Test
    fun seekReadSecondPiece() = runTest {
        logicalPieces[1].state.emit(PieceState.FINISHED)
        file.seek(16)
        assertEquals(16L, file.offset)
        file.readBytes().run {
            assertEquals(16, size)
            assertEquals("imply dummy text", String(this))
        }
    }

    @Test
    fun seekReadSecondPieceMiddle() = runTest {
        logicalPieces[1].state.emit(PieceState.FINISHED)
        file.seek(17)
        assertEquals(17L, file.offset)
        file.readBytes().run {
            assertEquals(15, size)
            assertEquals("mply dummy text", String(this))
        }
    }
}
