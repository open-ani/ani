/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.models

import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectAiringKind
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectAiringInfoTest {
    private var idCounter = 0

    private fun ep(
        sort: Int,
        airDate: PackedDate = PackedDate.Invalid,
    ): EpisodeInfo = EpisodeInfo(id = ++idCounter, sort = EpisodeSort(sort), airDate = airDate)

    @Test
    fun `empty episode list is upcoming`() {
        val info = SubjectAiringInfo.computeFromEpisodeList(emptyList(), PackedDate.Invalid)
        assertEquals(SubjectAiringKind.UPCOMING, info.kind)
        assertEquals(0, info.episodeCount)
        assertEquals(PackedDate.Invalid, info.airDate)
        assertEquals(null, info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(null, info.upcomingSort)
    }

    @Test
    fun `single episode upcoming`() {
        val eps = listOf(
            ep(3, PackedDate(8888, 1, 8 + 7 * 2)),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate.Invalid)
        assertEquals(SubjectAiringKind.UPCOMING, info.kind)
        assertEquals(1, info.episodeCount)
        assertEquals(PackedDate(8888, 1, 8 + 7 * 2), info.airDate)
        assertEquals(EpisodeSort(3), info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(EpisodeSort(3), info.upcomingSort)
    }

    @Test
    fun `all episodes are completed`() {
        val eps = listOf(
            ep(1, PackedDate(2023, 1, 8)),
            ep(2, PackedDate(2023, 1, 8 + 7)),
            ep(3, PackedDate(2023, 1, 8 + 7 * 2)),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate.Invalid)
        assertEquals(SubjectAiringKind.COMPLETED, info.kind)
        assertEquals(3, info.episodeCount)
        assertEquals(PackedDate(2023, 1, 8), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(EpisodeSort(3), info.latestSort)
        assertEquals(null, info.upcomingSort)
    }

    @Test
    fun `some episodes completed - some upcoming`() {
        val eps = listOf(
            ep(1, PackedDate(2023, 1, 8)),
            ep(2, PackedDate(2023, 1, 8 + 7)),
            ep(3, PackedDate(8888, 1, 8 + 7 * 2)),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate.Invalid)
        assertEquals(SubjectAiringKind.ON_AIR, info.kind)
        assertEquals(3, info.episodeCount)
        assertEquals(PackedDate(2023, 1, 8), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(EpisodeSort(2), info.latestSort)
        assertEquals(EpisodeSort(3), info.upcomingSort)
    }

    @Test
    fun `one episode but has invalid date - when subject has invalid air date`() {
        val eps = listOf(
            ep(1, PackedDate.Invalid),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate.Invalid)
        assertEquals(SubjectAiringKind.UPCOMING, info.kind)
        assertEquals(1, info.episodeCount)
        assertEquals(PackedDate.Invalid, info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(EpisodeSort(1), info.upcomingSort)
    }

    @Test
    fun `one episode but has invalid date - when subject is known broadcast`() {
        val eps = listOf(
            ep(1, PackedDate.Invalid),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate(2023, 1, 1))
        assertEquals(SubjectAiringKind.COMPLETED, info.kind)
        assertEquals(1, info.episodeCount)
        assertEquals(PackedDate(2023, 1, 1), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(null, info.upcomingSort)
    }

    @Test
    fun `one episode but has invalid date - when subject is known broadcast for 10+ years`() {
        val eps = listOf(
            ep(1, PackedDate.Invalid),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate(2000, 1, 1))
        assertEquals(SubjectAiringKind.COMPLETED, info.kind)
        assertEquals(1, info.episodeCount)
        assertEquals(PackedDate(2000, 1, 1), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(null, info.upcomingSort)
    }

    @Test
    fun `one episode has invalid date - when subject is known upcoming`() {
        val eps = listOf(
            ep(1, PackedDate.Invalid),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate(8888, 1, 1))
        assertEquals(SubjectAiringKind.UPCOMING, info.kind)
        assertEquals(1, info.episodeCount)
        assertEquals(PackedDate(8888, 1, 1), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(EpisodeSort(1), info.upcomingSort)
    }

    @Test
    fun `one episode upcoming and one invalid`() {
        val eps = listOf(
            ep(1, PackedDate(8888, 1, 8)),
            ep(2, PackedDate.Invalid),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate.Invalid)
        assertEquals(SubjectAiringKind.UPCOMING, info.kind)
        assertEquals(2, info.episodeCount)
        assertEquals(PackedDate(8888, 1, 8), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(null, info.latestSort)
        assertEquals(EpisodeSort(1), info.upcomingSort)
    }

    @Test
    fun `one episode completed and one invalid`() {
        val eps = listOf(
            ep(1, PackedDate(1000, 1, 8)),
            ep(2, PackedDate.Invalid),
        )
        val info = SubjectAiringInfo.computeFromEpisodeList(eps, PackedDate.Invalid)
        assertEquals(SubjectAiringKind.ON_AIR, info.kind)
        assertEquals(2, info.episodeCount)
        assertEquals(PackedDate(1000, 1, 8), info.airDate)
        assertEquals(EpisodeSort(1), info.firstSort)
        assertEquals(EpisodeSort(1), info.latestSort)
        assertEquals(EpisodeSort(2), info.upcomingSort)
    }
}