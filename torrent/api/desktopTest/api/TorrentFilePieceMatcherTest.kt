package me.him188.ani.app.torrent.api

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.api.pieces.indexes
import me.him188.ani.app.torrent.assertCoroutineSuspends
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class TorrentFilePieceMatcherTest : TorrentSessionSupport() {
    // 直接在 session 里测试

    @Test
    fun `file piece - calculation in ctor`() = runTest {
        withSession {
            assertCoroutineSuspends { getFiles() }
            setHandle {
                files.add(TestTorrentFile("1.mp4", 1024))
                // 没有提供 piece
            }
            assertFails { getFiles() }
        }
    }

    @Test
    fun `file piece - single match`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 2000))
                replacePieces {
                    piece(2000)
                }
            }

            getFiles().run {
                assertEquals(1, size)
                assertEquals("1.mp4", first().pathInTorrent)
                assertEquals(0L..<2000, first().pieces!!.single().indexes)
            }
        }
    }

    @Test
    fun `file piece - single empty`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 0))
            }

            getFiles().run {
                assertEquals(1, size)
                assertEquals("1.mp4", first().pathInTorrent)
                assertEquals(0, first().pieces!!.size)
            }
        }
    }

    @Test
    fun `file piece - multiple match`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 2000))
                files.add(TestTorrentFile("2.mp4", 3000))
                replacePieces {
                    piece(2000)
                    piece(3000)
                }
            }

            getFiles().run {
                get(0).run {
                    assertEquals("1.mp4", pathInTorrent)
                    assertEquals(0L..<2000, pieces!!.single().indexes)
                }
                get(1).run {
                    assertEquals("2.mp4", pathInTorrent)
                    assertEquals(2000L..<5000, pieces!!.single().indexes)
                }
            }
        }
    }

    @Test
    fun `file piece - multiple match with extra`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 2000))
                files.add(TestTorrentFile("2.mp4", 3000))
                replacePieces {
                    piece(2000)
                    piece(3000)
                    piece(3000)
                }
            }

            getFiles().run {
                get(0).run {
                    assertEquals("1.mp4", pathInTorrent)
                    assertEquals(0L..<2000, pieces!!.single().indexes)
                }
                get(1).run {
                    assertEquals("2.mp4", pathInTorrent)
                    assertEquals(2000L..<5000, pieces!!.single().indexes)
                }
            }
        }
    }

    @Test
    fun `file piece - single combine`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 5000))
                replacePieces {
                    piece(2000)
                    piece(3000)
                    piece(3000)
                }
            }

            getFiles().run {
                get(0).run {
                    assertEquals("1.mp4", pathInTorrent)
                    assertEquals(0L..<2000, pieces!![0].indexes)
                    assertEquals(2000L..<5000, pieces!![1].indexes)
                }
            }
        }
    }

    @Test
    fun `file piece - multiple combine`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 2000))
                files.add(TestTorrentFile("2.mp4", 3000))
                replacePieces {
                    piece(1000)
                    piece(1000)
                    piece(2000)
                    piece(1000)
                }
            }

            getFiles().run {
                get(0).run {
                    assertEquals(0L..<1000, pieces!![0].indexes)
                    assertEquals(1000L..<2000, pieces!![1].indexes)
                }
                get(1).run {
                    assertEquals(2000L..<4000, pieces!![0].indexes)
                    assertEquals(4000L..<5000, pieces!![1].indexes)
                }
            }
        }
    }

    @Test
    fun `file piece - multiple mix`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 2000))
                files.add(TestTorrentFile("2.mp4", 3000))
                replacePieces {
                    piece(1000) // 0..1000
                    piece(1500) // 1000..2500
                    piece(1500) // 2500..4000
                    piece(1000) // 4000..5000
                    check(build().sumOf { it.size } == 5000L)
                }
            }

            getFiles().run {
                get(0).run {
                    assertEquals(0L..<1000, pieces!![0].indexes)
                    assertEquals(1000L..<2500, pieces!![1].indexes)
                }
                get(1).run {
                    assertEquals(1000L..<2500L, pieces!![0].indexes)
                    assertEquals(2500L..<4000L, pieces!![1].indexes)
                    assertEquals(4000L..<5000L, pieces!![2].indexes)
                }
            }
        }
    }

    @Test
    fun `file piece - single size not enough`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("1.mp4", 5000))
                replacePieces {
                    piece(2000)
                    piece(2999)
                }
            }

            assertFails { getFiles() }
        }
    }

    @Test
    fun `file piece - second file size not enough`() = runTest {
        withSession {
            setHandle {
                files.add(TestTorrentFile("0.mp4", 2000))
                files.add(TestTorrentFile("1.mp4", 5000))
                replacePieces {
                    piece(2000)
                    piece(2000)
                    piece(2999)
                }
            }

            assertFails { getFiles() }
        }
    }
}