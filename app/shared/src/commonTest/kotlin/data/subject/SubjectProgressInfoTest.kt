package me.him188.ani.app.data.subject

import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.SubjectProgressInfo.Episode
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.datasources.api.PackedDate.Companion.Invalid
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.UnifiedCollectionType.DONE
import me.him188.ani.datasources.api.topic.UnifiedCollectionType.DROPPED
import me.him188.ani.datasources.api.topic.UnifiedCollectionType.WISH
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectProgressInfoTest {
    private fun ep(
        type: UnifiedCollectionType,
        sort: Int,
        isKnownCompleted: Boolean,
        airDate: PackedDate = Invalid,
        id: Int = sort,
    ): Episode = Episode(
        id, type, EpisodeSort(sort),
        airDate,
        isKnownCompleted,
    )

    private fun calculate(
        subjectStarted: Boolean,
        episodes: List<Episode>,
        subjectAirDate: PackedDate = Invalid,
    ): SubjectProgressInfo {
        return SubjectProgressInfo.calculate(
            subjectStarted,
            episodes,
            subjectAirDate,
        )
    }

    @Test
    fun `subject not started - no ep`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir(Invalid), continueWatchingStatus)
            assertEquals(null, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - no ep - with time`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(),
            subjectAirDate = PackedDate(2024, 8, 24),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir(PackedDate(2024, 8, 24)), continueWatchingStatus)
            assertEquals(null, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - one ep`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(WISH, 1, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir(Invalid), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first episode wish`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(WISH, 1, isKnownCompleted = false),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir(Invalid), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first ep done - second ep not completed`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Watched(0, EpisodeSort(1), Invalid), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first ep done - first ep not completed`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = false),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Watched(0, EpisodeSort(1), Invalid), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first ep done - second ep completed`() {
        calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Continue(1, EpisodeSort(2), EpisodeSort(1)), continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first episode wish`() {
        calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(WISH, 1, isKnownCompleted = false),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Start, continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first ep done - second ep not completed`() {
        calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Watched(0, EpisodeSort(1), Invalid), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first ep done - second ep completed`() {
        calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Continue(1, EpisodeSort(2), EpisodeSort(1)), continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first ep dropped - second ep completed`() {
        calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DROPPED, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Continue(1, EpisodeSort(2), EpisodeSort(1)), continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `all ep done`() {
        calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(DONE, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Done, continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }
}

