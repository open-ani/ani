package me.him188.ani.app.torrent.api

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.PiecesBuilder
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.handle.AniTorrentHandle
import me.him188.ani.app.torrent.api.handle.TorrentContents
import me.him188.ani.app.torrent.api.handle.TorrentFile
import me.him188.ani.app.torrent.api.handle.TorrentThread
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.assertCoroutineSuspends
import me.him188.ani.app.torrent.buildPieceList
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(TorrentThread::class)
internal class TorrentDownloadSessionTest : TorrentSessionSupport() {
    ///////////////////////////////////////////////////////////////////////////
    // file pieces
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `getFiles suspend until handle ready`() = runTest {
        withSession {
            assertCoroutineSuspends { getFiles() }
            setHandle {
                addFileAndPieces(TestTorrentFile("1.mp4", 1024))
            }
            getFiles().run {
                assertEquals(1, size)
                assertEquals("1.mp4", first().pathInTorrent)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // resume
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `no resume if no file handle created`() = runTest {
        withSession {
            setHandle(
                object : TestAniTorrentHandle() {
                    override fun resume() {
                        fail("Should not resume")
                    }

                    override fun pause() {
                        fail("Should not resume")
                    }
                },
            ) {
                addFileAndPieces(TestTorrentFile("1.mp4", 1024))
            }
        }
    }

    @Test
    fun `resume torrent by handle`() = runTest {
        withSession {
            var resumeCalled = 0
            val handle = setHandle(
                object : TestAniTorrentHandle() {
                    override fun resume() {
                        resumeCalled++
                    }

                    override fun pause() {
                        fail("Should not resume")
                    }
                },
            ) {
                addFileAndPieces(TestTorrentFile("1.mp4", 1024))
            }

            val file = getFiles().single()
            file.createHandle().use {
                it.resume()
            }
            listener.onUpdate(handle)
            assertTrue { resumeCalled > 1 } // 不一定是 1, 因为更新 file priority 也会有 resume
        }
    }
}

open class TestAniTorrentHandle(
    override val name: String = "test",
) : AniTorrentHandle {
    val trackers = mutableListOf<String>()

    val pieces = mutableListOf<TestPiece>()
    val files = mutableListOf<TorrentFile>()

    val fileProgresses by lazy {
        files.mapTo(mutableListOf()) {
            it to 0L
        }
    }

    @TorrentThread
    override val contents = object : TorrentContents {
        override fun createPieces(): List<Piece> = this@TestAniTorrentHandle.pieces.map { it.piece }
        override val files: List<TorrentFile> get() = this@TestAniTorrentHandle.files

        @TorrentThread
        override fun getFileProgresses(): List<Pair<TorrentFile, Long>> = fileProgresses
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

fun TestAniTorrentHandle.appendPieces(builderAction: PiecesBuilder.() -> Unit) {
    pieces.addAll(
        buildPieceList(
            initialOffset = pieces.lastOrNull()?.piece?.lastIndex?.plus(1) ?: 0,
            builderAction,
        ).map { TestPiece(it) },
    )
}

fun TestAniTorrentHandle.addFileAndPieces(
    file: TestTorrentFile,
) {
    files.add(file)
    appendPieces {
        piece(file.size)
    }
}

class TestPiece(
    var piece: Piece,
    var deadline: Int? = null,
)


class TestTorrentFile @OptIn(TorrentThread::class) constructor(
    override var path: String = "",
    override var size: Long = 0,
    @property:TorrentThread
    override var priority: FilePriority = FilePriority.NORMAL
) : TorrentFile 