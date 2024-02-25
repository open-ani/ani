package me.him188.ani.app.torrent.download

import me.him188.ani.app.torrent.buildPieceList
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TorrentDownloadControllerTest {
    class TestPiecePriorities : PiecePriorities {
        val downloadOnly = mutableListOf<List<Int>>()
        override fun downloadOnly(pieceIndexes: Collection<Int>) {
            downloadOnly.add(pieceIndexes.toList())
        }
    }

    private val priorities = TestPiecePriorities()

    private fun createPrioritizer() = TorrentDownloadController(
        buildPieceList {
            // 0
            piece(1024)
            piece(1024)
            piece(1024)
            piece(1024) // 4096

            // 4
            piece(1024)
            piece(1024)
            piece(1024)
            piece(1024)
            piece(1024)
            piece(1024)

            // 10
            piece(1024)
            piece(1024)
            piece(1024)
            piece(1024)
        },
        priorities,
        windowSize = 2,
        headerSize = 4096,
        footerSize = 4096,
    )

    private fun createPrioritizerSequential(): TorrentDownloadController {
        return createPrioritizer().apply {
            assertIs<State.Metadata>(state)
            for (i in listOf(0, 1, 2, 3, 10, 11, 12, 13)) {
                onPieceDownloaded(i)
            }
        }
    }

    @Test
    fun `request header and footer`() {
        val prioritizer = createPrioritizer()
        val state = prioritizer.state
        assertIs<State.Metadata>(state)
        assertEquals(listOf(0, 1, 2, 3, 10, 11, 12, 13), state.requestedPieces)
    }

    @Test
    fun `stay in Metadata state when not all pieces arrive`() {
        val prioritizer = createPrioritizer()
        assertIs<State.Metadata>(prioritizer.state)
        for (i in 0 until 3) {
            prioritizer.onPieceDownloaded(i)
        }
        assertIs<State.Metadata>(prioritizer.state)
    }

    @Test
    fun `go to Sequential state when all pieces arrive`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)

            assertEquals(listOf(4, 5), priorities.downloadOnly.single())
        }
    }

    @Test
    fun `stay in Sequential state when non-head piece finish`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)
        }

        prioritizer.onPieceDownloaded(5)
        assertIs<State.Sequential>(prioritizer.state)
    }

    @Test
    fun `stay in Sequential state when head piece finish`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)
        }

        prioritizer.onPieceDownloaded(4)
        assertIs<State.Sequential>(prioritizer.state)
    }


    @Test
    fun `Sequential state startIndex and lastIndex`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(4, startIndex)
            assertEquals(9, lastIndex)
        }
    }

    @Test
    fun `finish when all pieces are downloaded`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)
        }

        for (i in 4..9) {
            prioritizer.onPieceDownloaded(i)
            println(prioritizer.state)
        }

        prioritizer.state.run {
            assertIs<State.Finished>(this)
            assertEquals(listOf(), downloadingPieces)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // window
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `finishing non-head piece does not advance window`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)
            assertEquals(4, currentWindowStart)
            assertEquals(5, currentWindowEnd)
        }

        prioritizer.onPieceDownloaded(5)

        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4), downloadingPieces)
            assertEquals(4, currentWindowStart)
            assertEquals(5, currentWindowEnd)
        }
    }

    @Test
    fun `finishing head piece advance window`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)
            assertEquals(4, currentWindowStart)
            assertEquals(5, currentWindowEnd)
        }

        prioritizer.onPieceDownloaded(4)

        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(5, 6), downloadingPieces)
            assertEquals(5, currentWindowStart)
            assertEquals(6, currentWindowEnd)
        }
    }

    @Test
    fun `finishing head piece advance window 2`() {
        val prioritizer = createPrioritizerSequential()
        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(4, 5), downloadingPieces)
            assertEquals(4, currentWindowStart)
            assertEquals(5, currentWindowEnd)
        }

        prioritizer.onPieceDownloaded(4)
        prioritizer.onPieceDownloaded(5)

        prioritizer.state.run {
            assertIs<State.Sequential>(this)
            assertEquals(listOf(6, 7), downloadingPieces)
            assertEquals(6, currentWindowStart)
            assertEquals(7, currentWindowEnd)
        }
    }
}