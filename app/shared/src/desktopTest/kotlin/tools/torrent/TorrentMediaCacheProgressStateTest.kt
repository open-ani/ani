package me.him188.ani.app.tools.torrent

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.ui.framework.takeSnapshot
import me.him188.ani.app.videoplayer.ui.state.ChunkState
import kotlin.test.Test
import kotlin.test.assertEquals

class TorrentMediaCacheProgressStateTest {
    private val pieces = Piece.buildPieces(16, 0) {
        1_000
    }.apply {
        assertEquals(16, size)
    }
    private val isFinished = false


    @Test
    fun `initial state`() {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        assertEquals(16, cacheProgress.chunks.size)
        assertEquals(1 / 16f, cacheProgress.chunks[0].weight)
        assertEquals(ChunkState.NONE, cacheProgress.chunks[0].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks.last().state)
    }

    @Test
    fun `update no change`() = runTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        cacheProgress.update()
        assertEquals(16, cacheProgress.chunks.size)
        assertEquals(1 / 16f, cacheProgress.chunks[0].weight)
        assertEquals(ChunkState.NONE, cacheProgress.chunks[0].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks.last().state)
    }

    @Test
    fun `update first finish`() = runTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        pieces.first().state.value = PieceState.FINISHED
        cacheProgress.update()
        assertEquals(ChunkState.DONE, cacheProgress.chunks[0].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks[1].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks.last().state)
    }

    @Test
    fun `update last finish`() = runTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        pieces.last().state.value = PieceState.FINISHED
        cacheProgress.update()
        assertEquals(ChunkState.NONE, cacheProgress.chunks[0].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks[1].state)
        assertEquals(ChunkState.DONE, cacheProgress.chunks.last().state)
    }

    @Test
    fun `update middle finish`() = runTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        pieces[5].state.value = PieceState.FINISHED
        cacheProgress.update()
        assertEquals(ChunkState.DONE, cacheProgress.chunks[5].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks[0].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks[1].state)
        assertEquals(ChunkState.NONE, cacheProgress.chunks.last().state)
    }

    @Test
    fun `update does not increment version if there is no change`() = runComposeStateTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        assertEquals(0, cacheProgress.version)
        cacheProgress.update()
        takeSnapshot()
        assertEquals(0, cacheProgress.version)
    }

    @Test
    fun `update increment version if first change`() = runComposeStateTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        assertEquals(0, cacheProgress.version)
        pieces.first().state.value = PieceState.FINISHED
        cacheProgress.update()
        takeSnapshot()
        assertEquals(1, cacheProgress.version)
    }

    @Test
    fun `update increment version if last change`() = runComposeStateTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        assertEquals(0, cacheProgress.version)
        pieces.last().state.value = PieceState.FINISHED
        cacheProgress.update()
        takeSnapshot()
        assertEquals(1, cacheProgress.version)
    }

    @Test
    fun `update increment version only once if first change`() = runComposeStateTest {
        val cacheProgress = TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
        assertEquals(0, cacheProgress.version)
        pieces.first().state.value = PieceState.FINISHED
        cacheProgress.update()
        takeSnapshot()
        assertEquals(1, cacheProgress.version)
        cacheProgress.update()
        takeSnapshot()
        assertEquals(1, cacheProgress.version)
    }

}