package me.him188.ani.app.data.media.fetch

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.data.media.framework.TestMediaList
import me.him188.ani.app.data.media.instance.MediaSourceInstance
import me.him188.ani.app.data.media.instance.createTestMediaSourceInstance
import me.him188.ani.app.torrent.assertCoroutineSuspends
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.TestHttpMediaSource
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

/**
 * @see MediaFetcher
 */
class MediaFetcherTest {
    private fun createFetcher(
        vararg instances: MediaSourceInstance
    ): MediaSourceMediaFetcher {
        return MediaSourceMediaFetcher(
            { MediaFetcherConfig.Default },
            listOf(*instances)
        )
    }

    private val request1 = MediaFetchRequest(
        subjectId = "123123",
        episodeId = "1231231",
        subjectNames = setOf("夜晚的水母不会游泳"),
        episodeSort = EpisodeSort("03"),
        episodeName = "测试剧集2",
    )

    ///////////////////////////////////////////////////////////////////////////
    // Completeness
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `toList flow does not complete`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource()))
            .newSession(request1)
        assertCoroutineSuspends {
            session.cumulativeResults.toList()
        }

        session.awaitCompletedResults()

        assertCoroutineSuspends {
            session.hasCompleted.toList()
        }
    }

    @Test
    fun `hasCompleted flow does not complete`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource()))
            .newSession(request1)
        assertCoroutineSuspends {
            session.hasCompleted.toList()
        }

        session.awaitCompletedResults()

        assertCoroutineSuspends {
            session.hasCompleted.toList()
        }
    }

    @Test
    fun `collect hasCompleted does not start fetch`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource(
            fetch = {
                fail("Should not fetch")
            }
        ))).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        assertIs<MediaSourceFetchState.Idle>(res.state.value)
        assertEquals(false, session.hasCompleted.first())
    }

    ///////////////////////////////////////////////////////////////////////////
    // hasCompleted
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `hasCompleted is initially true if no source`() = runTest {
        val session = createFetcher().newSession(request1)
        assertEquals(0, session.results.size)
        assertEquals(true, session.hasCompleted.first())
    }

    @Test
    fun `hasCompleted is initially false when all sources are enabled`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource())).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        assertIs<MediaSourceFetchState.Idle>(res.state.value)
        assertEquals(false, session.hasCompleted.first())
    }

    @Test
    fun `hasCompleted is initially true when all sources are disabled`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(TestHttpMediaSource(), isEnabled = false),
            createTestMediaSourceInstance(TestHttpMediaSource(), isEnabled = false),
        ).newSession(request1)
        assertEquals(2, session.results.size)
        assertIs<MediaSourceFetchState.Disabled>(session.results.first().state.value)
        assertIs<MediaSourceFetchState.Disabled>(session.results.toList()[1].state.value)
        assertEquals(true, session.hasCompleted.first())
    }

    @Test
    fun `hasCompleted is initially false when some sources are disabled`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(TestHttpMediaSource(), isEnabled = false),
            createTestMediaSourceInstance(TestHttpMediaSource()),
        ).newSession(request1)
        assertEquals(2, session.results.size)
        assertIs<MediaSourceFetchState.Disabled>(session.results.first().state.value)
        assertIs<MediaSourceFetchState.Idle>(session.results[1].state.value)
        assertEquals(false, session.hasCompleted.first())
    }

    ///////////////////////////////////////////////////////////////////////////
    // cumulativeResults
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `awaitCompletedResults from one source`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource(
            fetch = {
                SinglePagePagedSource {
                    TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                }
            }
        ))).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        assertIs<MediaSourceFetchState.Idle>(res.state.value)
        assertEquals(5, session.awaitCompletedResults().size)
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
    }

    // 从两个不同的源获取数据, 但是数据是相同的, 需要去重
    @Test
    fun `awaitCompletedResults from two sources distinct`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(TestHttpMediaSource(
                fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }
            )),
            createTestMediaSourceInstance(TestHttpMediaSource(
                fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }
            )),
        ).newSession(request1)
        assertEquals(2, session.results.size)
        val res = session.results.first()
        assertIs<MediaSourceFetchState.Idle>(res.state.value)
        assertEquals(5, session.awaitCompletedResults().size) // because the same media is returned from both sources
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
    }

    @Test
    fun `awaitCompletedResults from two sources`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(TestHttpMediaSource(
                fetch = {
                    SinglePagePagedSource {
                        TestMediaList.take(1).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }
            )),
            createTestMediaSourceInstance(TestHttpMediaSource(
                fetch = {
                    SinglePagePagedSource {
                        TestMediaList.drop(1).take(1).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }
            )),
        ).newSession(request1)
        assertEquals(2, session.results.size)
        val res = session.results.first()
        assertIs<MediaSourceFetchState.Idle>(res.state.value)
        assertEquals(2, session.awaitCompletedResults().size)
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
    }

    @Test
    fun `initial empty cumulative list`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource(fetch = {
            SinglePagePagedSource {
                TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
            }
        }))).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.cumulativeResults.first()
        assertEquals(0, res.size)
    }

    @Test
    fun `source result is shared and has replay`() = runTest {
        val session = createFetcher(createTestMediaSourceInstance(TestHttpMediaSource(fetch = {
            SinglePagePagedSource {
                TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
            }
        }))).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        assertEquals(5, session.awaitCompletedResults().size)
        assertEquals(5, session.cumulativeResults.first().size)
        assertEquals(5, res.results.first().size)
    }

    ///////////////////////////////////////////////////////////////////////////
    // source 之间不影响
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `collecting one source does not start the other`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.take(2).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            ),
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    fail("Should not fetch")
                })
            )
        ).newSession(request1)
        assertEquals(2, session.results.size)
        val res = session.results.first()
        assertEquals(2, res.awaitCompletedResults().size)

        val res2 = session.results[1]
        assertIs<MediaSourceFetchState.Idle>(res2.state.value)
    }

    ///////////////////////////////////////////////////////////////////////////
    // disable sources
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `disable source`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }),
                isEnabled = false
            )
        ).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        assertIs<MediaSourceFetchState.Disabled>(res.state.value)
        assertEquals(0, session.awaitCompletedResults().size)
        assertIs<MediaSourceFetchState.Disabled>(res.state.value)
    }

    @Test
    fun `collect from enabled source but not disabled`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }),
                isEnabled = false
            ),
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.take(3).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }),
            )
        ).newSession(request1)
        assertEquals(2, session.results.size)
        assertIs<MediaSourceFetchState.Disabled>(session.results.first().state.value)
        assertEquals(3, session.awaitCompletedResults().size)
        assertIs<MediaSourceFetchState.Succeed>(session.results[1].state.value)
    }

    @Test
    fun `hasCompleted can be true if all sources are disabled`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }),
                isEnabled = false
            ),
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.take(3).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }),
                isEnabled = false
            )
        ).newSession(request1)
        assertEquals(2, session.results.size)
        assertIs<MediaSourceFetchState.Disabled>(session.results.first().state.value)
        assertIs<MediaSourceFetchState.Disabled>(session.results[1].state.value)
        assertEquals(0, session.awaitCompletedResults().size)
        assertEquals(true, session.hasCompleted.first())
    }

    ///////////////////////////////////////////////////////////////////////////
    // resultsIfEnabled
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `resultsIfEnabled is empty if source is disabled`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                }),
                isEnabled = false
            )
        ).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Disabled>(res.state.value)
        assertEquals(0, res.resultsIfEnabled.first().size)
    }

    @Test
    fun `resultsIfEnabled is the same as results if source is enabled`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            )
        ).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
        assertEquals(5, res.resultsIfEnabled.first().size)
        assertEquals(5, res.results.first().size)
    }

    ///////////////////////////////////////////////////////////////////////////
    // restart
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `fetch is called once and then cached`() = runTest {
        val fetchCalled = AtomicInteger(0)
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    fetchCalled.incrementAndGet()
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            )
        ).newSession(request1)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
        assertEquals(1, fetchCalled.get())
        session.awaitCompletedResults()
        assertEquals(1, fetchCalled.get())
        res.results.first()
        assertEquals(1, fetchCalled.get())
    }

    @Test
    fun `restart calls fetch again`() = runTest {
        val fetchCalled = AtomicInteger(0)
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    fetchCalled.incrementAndGet()
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            )
        ).newSession(request1)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
        assertEquals(1, fetchCalled.get())
        res.restart()
        session.awaitCompletedResults()
        assertEquals(2, fetchCalled.get())
    }

    @Test
    fun `restart resets state to be Idle`() = runTest {
        val fetchCalled = AtomicInteger(0)
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    fetchCalled.incrementAndGet()
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            )
        ).newSession(request1)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
        assertEquals(1, fetchCalled.get())
        res.restart()
        assertIs<MediaSourceFetchState.Idle>(res.state.value)
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Completed>(res.state.value)
        assertEquals(2, fetchCalled.get())
    }

    @Test
    fun `restart does not clear the existing result immediately`() = runTest {
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    SinglePagePagedSource {
                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            )
        ).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
        assertEquals(5, res.results.first().size)

        res.restart()
        assertEquals(5, res.results.first().size)
    }

    @Test
    fun `new result is made available to cumulativeResults`() = runTest {
        val fetchCalled = AtomicInteger(0)
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    if (fetchCalled.incrementAndGet() == 1) {
                        SinglePagePagedSource {
                            TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                        }
                    } else {
                        SinglePagePagedSource {
                            TestMediaList.take(3).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                        }
                    }
                })
            )
        ).newSession(request1)
        assertEquals(1, session.results.size)
        val res = session.results.first()
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res.state.value)
        assertEquals(5, res.results.first().size)

        res.restart()
        session.awaitCompletedResults()
        assertEquals(3, res.results.first().size)
        assertEquals(3, session.cumulativeResults.first().size)
    }

    @Test
    fun `restarting one source does not restart other completed ones`() = runTest {
        val firstFetchCalled = AtomicInteger(0)
        val secondFetchCalled = AtomicInteger(0)
        val session = createFetcher(
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    firstFetchCalled.incrementAndGet()
                    SinglePagePagedSource {
                        TestMediaList.take(2).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            ),
            createTestMediaSourceInstance(
                TestHttpMediaSource(fetch = {
                    secondFetchCalled.incrementAndGet()
                    SinglePagePagedSource {
                        TestMediaList.drop(2).take(3).map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                    }
                })
            )
        ).newSession(request1)
        assertEquals(2, session.results.size)
        val res1 = session.results[0]
        val res2 = session.results[1]
        session.awaitCompletedResults()
        assertIs<MediaSourceFetchState.Succeed>(res1.state.value)
        assertIs<MediaSourceFetchState.Succeed>(res2.state.value)
        assertEquals(2, res1.results.first().size)
        assertEquals(3, res2.results.first().size)
        assertEquals(1, firstFetchCalled.get())
        assertEquals(1, secondFetchCalled.get())

        res1.restart()
        assertEquals(1, firstFetchCalled.get())
        assertEquals(1, secondFetchCalled.get())

        session.awaitCompletedResults()
        assertEquals(2, firstFetchCalled.get())
        assertEquals(1, secondFetchCalled.get())
        
        assertEquals(2, res1.results.first().size)
        assertEquals(3, res2.results.first().size)
        assertEquals(5, session.cumulativeResults.first().size)
    }
}
