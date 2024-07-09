package me.him188.ani.app.data.subject

import me.him188.ani.app.data.models.PackedDate
import me.him188.ani.app.data.models.episode.EpisodeCollections
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EpisodeCollectionsTest {

    @Test
    @Disabled
    fun `subject completed after 14 days`() {
        assertTrue {
            EpisodeCollections.isSubjectCompleted(
                sequenceOf(
                    PackedDate(2023, 10, 4),
                    PackedDate(2023, 10, 11),
                ),
                now = PackedDate(2023, 10, 11 + 14),
            )
        }
    }

    @Test
    fun `subject not completed within 14 days`() {
        assertFalse {
            EpisodeCollections.isSubjectCompleted(
                sequenceOf(
                    PackedDate(2023, 10, 4),
                    PackedDate(2023, 10, 11),
                ),
                now = PackedDate(2023, 10, 11 + 13),
            )
        }
    }

    @Test
    fun `subject not completed before first start`() {
        assertFalse {
            EpisodeCollections.isSubjectCompleted(
                sequenceOf(
                    PackedDate(2023, 10, 4),
                    PackedDate(2023, 10, 11),
                ),
                now = PackedDate(2022, 10, 11),
            )
        }
    }

    @Test
    fun `subject not completed before last start`() {
        assertFalse {
            EpisodeCollections.isSubjectCompleted(
                sequenceOf(
                    PackedDate(2023, 10, 4),
                    PackedDate(2023, 10, 11),
                ),
                now = PackedDate(2022, 10, 10),
            )
        }
    }
}