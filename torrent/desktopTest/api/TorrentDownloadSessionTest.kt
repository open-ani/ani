package me.him188.ani.app.torrent.api

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.PiecesBuilder
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.assertCoroutineSuspends
import me.him188.ani.app.torrent.buildPieceList
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TorrentDownloadSessionTest : TorrentSessionSupport() {
    ///////////////////////////////////////////////////////////////////////////
    // file pieces
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `getFiles suspend until handle ready`() = runTest {
        withSession {
            assertCoroutineSuspends { getFiles() }
            setHandle {
                files.add(TestTorrentFile("1.mp4", 1024))
            }
            getFiles().run {
                assertEquals(1, size)
                assertEquals("1.mp4", first().pathInTorrent)
            }
        }
    }


}

open class TestAniTorrentHandle(
    override val name: String,
) : AniTorrentHandle {
    val trackers = mutableListOf<String>()

    val pieces = mutableListOf<TestPiece>()
    val files = mutableListOf<TorrentFile>()

    override val contents = object : TorrentContents {
        override fun createPieces(): List<Piece> = this@TestAniTorrentHandle.pieces.map { it.piece }
        override val files: List<TorrentFile> get() = this@TestAniTorrentHandle.files
    }

    override fun addTracker(url: String) {
        trackers.add(url)
    }

    var isResumed = false

    override fun resume() {
        isResumed = true
    }

    override fun pause() {
        isResumed = false
    }

    override fun setPieceDeadline(pieceIndex: Int, deadline: Int) {
        check(pieceIndex in this.pieces.map { it.piece.pieceIndex }) { "Piece $pieceIndex not found" }
        this.pieces.first { it.piece.pieceIndex == pieceIndex }.deadline = deadline
    }
}

fun TestAniTorrentHandle.replacePieces(builderAction: PiecesBuilder.() -> Unit) {
    pieces.clear()
    pieces.addAll(buildPieceList(builderAction).map { TestPiece(it) })
}

class TestPiece(
    var piece: Piece,
    var deadline: Int? = null,
)


class TestTorrentFile(
    override var path: String = "",
    override var size: Long = 0,
    override var priority: FilePriority = FilePriority.NORMAL
) : TorrentFile 