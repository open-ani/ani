package me.him188.ani.app.torrent.api

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.handle.PieceFinishedEvent
import me.him188.ani.app.torrent.api.handle.TorrentFinishedEvent
import me.him188.ani.app.torrent.api.handle.TorrentThread
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(TorrentThread::class)
internal class TorrentFileHandleTest : TorrentSessionSupport() {

    @Test
    fun `not finished on creation`() = runTest {
        withSession {
            setHandle {
                addFileAndPieces(TestTorrentFile("1.mp4", 1024))
            }

            getFiles().first().createHandle().use { handle ->
                assertFalse(handle.entry.stats.isFinished.first())
            }
        }
    }

    @Test
    fun `not finished when one piece done`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            val file = getFiles().first()
            val fileHandle = file.createHandle()
            assertFalse(fileHandle.entry.stats.isFinished.first())
            listener.onEvent(PieceFinishedEvent(handle.name, 0))
            assertFalse(fileHandle.entry.stats.isFinished.first())
            fileHandle.close()
        }
    }

    @Test
    fun `finished when all pieces done`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            val file = getFiles().first()
            val fileHandle = file.createHandle()
            fileHandle.entry.stats.run {
                assertFalse(isFinished.first())
            }
            listener.onEvent(PieceFinishedEvent(handle.name, 0))
            fileHandle.entry.stats.run {
                assertFalse(isFinished.first())
            }
            listener.onEvent(PieceFinishedEvent(handle.name, 1))
            fileHandle.entry.stats.run {
                assertTrue(isFinished.first())
            }
            fileHandle.close()
        }
    }

    @Test
    fun `overallStats finished`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            val file = getFiles().first()
            val fileHandle = file.createHandle()
            overallStats.run {
                assertFalse(isFinished.first())
            }
            listener.onEvent(PieceFinishedEvent(handle.name, 0))
            overallStats.run {
                assertFalse(isFinished.first())
            }
            listener.onEvent(PieceFinishedEvent(handle.name, 1))
            overallStats.run {
                assertTrue(isFinished.first())
            }
            fileHandle.close()
        }
    }

    @Test
    fun stats() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            val file = getFiles().first()
            val fileHandle = file.createHandle()
            fileHandle.entry.stats.run {
                assertFalse(isFinished.first())
                assertEquals(0f, progress.first())
                assertEquals(0L, downloadedBytes.first())
                assertEquals(1000, totalBytes.first())
            }
            listener.onEvent(PieceFinishedEvent(handle.name, 0))
            fileHandle.entry.stats.run {
                assertFalse(isFinished.first())
                assertEquals(0.5f, progress.first())
                assertEquals(500L, downloadedBytes.first())
                assertEquals(1000, totalBytes.first())
            }
            listener.onEvent(PieceFinishedEvent(handle.name, 1))
            fileHandle.entry.stats.run {
                assertTrue(isFinished.first())
                assertEquals(1f, progress.first())
                assertEquals(1000L, downloadedBytes.first())
                assertEquals(1000, totalBytes.first())
            }
            fileHandle.close()
        }
    }

    @Test
    fun `TorrentFinishedEvent finishes ongoing tasks with all pieces not done`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            handle.fileProgresses[0] = handle.fileProgresses[0].copy(second = 1000)

            val file = getFiles().first()
            val fileHandle = file.createHandle()

            listener.onEvent(TorrentFinishedEvent(handle.name, lazyOf(handle)))

            assertEquals(1000L, fileHandle.entry.stats.downloadedBytes.first())
            assertTrue(fileHandle.entry.stats.isFinished.first())
            assertEquals(1f, fileHandle.entry.stats.progress.first())
            assertTrue(fileHandle.entry.pieces!!.all { it.state.value == PieceState.FINISHED })

            fileHandle.close()
        }
    }

    @Test
    fun `TorrentFinishedEvent finishes ongoing tasks with some pieces already done`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            handle.pieces[0].piece.state.value = PieceState.FINISHED
            handle.fileProgresses[0] = handle.fileProgresses[0].copy(second = 1000)

            val file = getFiles().first()
            val fileHandle = file.createHandle()

            listener.onEvent(TorrentFinishedEvent(handle.name, lazyOf(handle)))

            assertEquals(1000L, fileHandle.entry.stats.downloadedBytes.first())
            assertTrue(fileHandle.entry.stats.isFinished.first())
            assertEquals(1f, fileHandle.entry.stats.progress.first())
            assertTrue(fileHandle.entry.pieces!!.all { it.state.value == PieceState.FINISHED })

            fileHandle.close()
        }
    }

    @Test
    fun `TorrentFinishedEvent finishes ongoing tasks with all pieces already done`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            handle.pieces.forEach { it.piece.state.value = PieceState.FINISHED }
            handle.fileProgresses[0] = handle.fileProgresses[0].copy(second = 1000)

            val file = getFiles().first()
            val fileHandle = file.createHandle()

            listener.onEvent(TorrentFinishedEvent(handle.name, lazyOf(handle)))

            assertEquals(1000L, fileHandle.entry.stats.downloadedBytes.first())
            assertTrue(fileHandle.entry.stats.isFinished.first())
            assertEquals(1f, fileHandle.entry.stats.progress.first())
            assertTrue(fileHandle.entry.pieces!!.all { it.state.value == PieceState.FINISHED })

            fileHandle.close()
        }
    }

    @Test
    fun `TorrentFinishedEvent does not finish task if file bytes not match`() = runTest {
        withSession {
            val handle = setHandle {
                files.add(TestTorrentFile("1.mp4", 1000))
                appendPieces {
                    piece(500)
                    piece(500)
                }
            }

            handle.fileProgresses[0] = handle.fileProgresses[0].copy(second = 500)

            val file = getFiles().first()
            val fileHandle = file.createHandle()

            listener.onEvent(TorrentFinishedEvent(handle.name, lazyOf(handle)))

            assertEquals(0L, fileHandle.entry.stats.downloadedBytes.first())
            assertFalse(fileHandle.entry.stats.isFinished.first())
            assertEquals(0f, fileHandle.entry.stats.progress.first())
            assertTrue(fileHandle.entry.pieces!!.all { it.state.value != PieceState.FINISHED })

            fileHandle.close()
        }
    }
}