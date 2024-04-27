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
import kotlin.test.assertFailsWith

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
        assertEquals(0, file.findPieceIndex(0))
        assertEquals(0, file.findPieceIndex(2))
        assertEquals(0, file.findPieceIndex(15))
        assertEquals(1, file.findPieceIndex(16))
        assertEquals(1, file.findPieceIndex(20))
        assertEquals(sampleTextByteArray.size / 16, file.findPieceIndex(sampleTextByteArray.lastIndex.toLong()))
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


    @Test
    fun `buffer single finished pieces, initial zero`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(logicalPieces[0].size, file.computeMaxBufferSize(0, 100000))
    }

    @Test
    fun `buffer single finished pieces, from intermediate`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(logicalPieces[0].size - 10, file.computeMaxBufferSize(10, 100000))
    }

    @Test
    fun `buffer multiple finished pieces, from intermediate`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        logicalPieces[1].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(logicalPieces[0].size - 0 + logicalPieces[0].size, file.computeMaxBufferSize(0, 100000))
        assertEquals(logicalPieces[0].size - 10 + logicalPieces[0].size, file.computeMaxBufferSize(10, 100000))
    }

    @Test
    fun `buffer zero byte (corner case)`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(0, file.computeMaxBufferSize(logicalPieces[0].size, 100000))
    }

    @Test
    fun `computeMaxBufferSize starting from piece not finished`() = runTest {
        // all pieces not finished
        assertEquals(0, file.computeMaxBufferSize(0, 100000))
    }

    ///////////////////////////////////////////////////////////////////////////
    // Error cases
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `seek negative offset`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            file.seek(-1)
        }
    }

    @Test
    fun `seek negative buffer`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            file.seek(1, -1)
        }
    }

    @Test
    fun `read negative length`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            file.read(ByteArray(1), 1, -1)
        }
    }

    @Test
    fun `read negative offset`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            file.read(ByteArray(1), -1, 1)
        }
    }

    @Test
    fun `seek over size`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            file.seek(Long.MAX_VALUE)
        }
    }

    @Test
    fun `read closed`() = runTest {
        file.close()
        assertFailsWith<IllegalStateException> {
            file.read(ByteArray(1), 1, 1)
        }
    }

    @Test
    fun `seek closed`() = runTest {
        file.close()
        assertFailsWith<IllegalStateException> {
            file.seek(10)
        }
    }
}
