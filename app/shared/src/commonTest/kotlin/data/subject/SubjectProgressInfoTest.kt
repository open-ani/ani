package me.him188.ani.app.data.subject

import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.SubjectProgressInfo.Episode
import me.him188.ani.datasources.api.EpisodeSort
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
        id: Int = sort,
    ): Episode = Episode(id, type, EpisodeSort(sort), isKnownCompleted)

    @Test
    fun `subject not started - no ep`() {
        SubjectProgressInfo.calculate(
            subjectStarted = false,
            episodes = listOf(),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir, continueWatchingStatus)
            assertEquals(null, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - one ep`() {
        SubjectProgressInfo.calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(WISH, 1, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir, continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first episode wish`() {
        SubjectProgressInfo.calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(WISH, 1, isKnownCompleted = false),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.NotOnAir, continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first ep done - second ep not completed`() {
        SubjectProgressInfo.calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Watched(0, EpisodeSort(1)), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `subject not started - first ep done - second ep completed`() {
        SubjectProgressInfo.calculate(
            subjectStarted = false,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Continue(1, EpisodeSort(2)), continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first episode wish`() {
        SubjectProgressInfo.calculate(
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
        SubjectProgressInfo.calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = false),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Watched(0, EpisodeSort(1)), continueWatchingStatus)
            assertEquals(1, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first ep done - second ep completed`() {
        SubjectProgressInfo.calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DONE, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Continue(1, EpisodeSort(2)), continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `first ep dropped - second ep completed`() {
        SubjectProgressInfo.calculate(
            subjectStarted = true,
            episodes = listOf(
                ep(DROPPED, 1, isKnownCompleted = true),
                ep(WISH, 2, isKnownCompleted = true),
            ),
        ).run {
            assertEquals(ContinueWatchingStatus.Continue(1, EpisodeSort(2)), continueWatchingStatus)
            assertEquals(2, nextEpisodeIdToPlay)
        }
    }

    @Test
    fun `all ep done`() {
        SubjectProgressInfo.calculate(
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

