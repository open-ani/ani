package me.him188.ani.app.torrent.api

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
}