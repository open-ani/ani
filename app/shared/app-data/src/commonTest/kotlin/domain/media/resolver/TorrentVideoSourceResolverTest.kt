/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.resolver

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.test.TestFactory
import me.him188.ani.test.dynamicTest
import me.him188.ani.test.permuted
import me.him188.ani.test.runDynamicTests
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SelectVideoFileAnitorrentEntryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `can select single`() {
        val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
            listOf("[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv"),
            { this },
            episodeTitles = listOf("终末列车去往何方?"),
            episodeSort = EpisodeSort(4),
            episodeEp = null,
        )
        assertNotNull(selected)
    }

    @Test
    fun `case from issue 813`() {
        val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
            listOf("[MingY] Senpai wa Otokonoko [06][1080p][CHS&JPN].mp4"),
            { this },
            episodeTitles = """
                - 前辈是男孩子
                - 先輩はおとこのこ
                - 学姐是男孩
                - 前辈是伪娘
                - Senpai wa Otokonoko
                - Senpai Is an Otokonoko
            """.trimIndent().split("\n").map { it.trim().removePrefix("- ") },
            episodeSort = EpisodeSort(6),
            episodeEp = EpisodeSort(6),
        )
        assertNotNull(selected)
    }

    @Test
    fun `can select from multiple by episodeSort`() {
        val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
            json.decodeFromString(
                ListSerializer(String.serializer()),
                """
                [
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 02 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 03 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 05 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv"
                ]
            """.trimIndent(),
            ),
            { this },
            episodeTitles = listOf("终末列车去往何方?"),
            episodeSort = EpisodeSort(4),
            episodeEp = null,
        )
        assertEquals(
            "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
            selected,
        )
    }

    @Test
    fun `can select from multiple by episodeEp`() {
        val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
            json.decodeFromString(
                ListSerializer(String.serializer()),
                """
                [
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 02 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 03 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 05 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv"
                ]
            """.trimIndent(),
            ),
            { this },
            episodeTitles = listOf("终末列车去往何方?"),
            episodeSort = EpisodeSort(26),
            episodeEp = EpisodeSort(4),
        )
        assertEquals(
            "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
            selected,
        )
    }

    @Test
    fun `select by sort than by ep`() {
        val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
            json.decodeFromString(
                ListSerializer(String.serializer()),
                """
                [
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 02 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 03 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 05 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv"
                ]
            """.trimIndent(),
            ),
            { this },
            episodeTitles = listOf("终末列车去往何方?"),
            episodeSort = EpisodeSort(4),
            episodeEp = EpisodeSort(2),
        )
        assertEquals(
            "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
            selected,
        )
    }

    @Test
    fun `can select from multiple by single title match`() {
        val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
            json.decodeFromString(
                ListSerializer(String.serializer()),
                """
                [
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 02 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 03 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 04 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
                    "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 05 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv"
                ]
            """.trimIndent(),
            ),
            { this },
            episodeTitles = listOf("Shuumatsu Train Doko e Iku - 03"),
            episodeSort = EpisodeSort(26),
            episodeEp = EpisodeSort(4),
        )
        assertEquals(
            "[Nekomoe kissaten&LoliHouse] Shuumatsu Train Doko e Iku - 03 [WebRip 1080p HEVC-10bit AAC ASSx2].mkv",
            selected,
        )
    }

    @TestFactory
    fun `select normal over PV`() = runDynamicTests(
        listOf(
            "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
            "[DBD-Raws][未来日记] PV [01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记] PV01 [1080P][BDRip][HEVC-10bit][FLAC].mkv",
        ).permuted().mapIndexed { index, list ->
            dynamicTest(index.toString()) {
                val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
                    list,
                    { this },
                    episodeTitles = listOf("未来日记"),
                    episodeSort = EpisodeSort(1),
                    episodeEp = null,
                )
                assertEquals(
                    "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
                    selected,
                    message = list.toString(),
                )
            }
        },
    )

    @TestFactory
    fun `select SP over PV`() = runDynamicTests(
        listOf(
            "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
            "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][SP][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        ).permuted().mapIndexed { index, list ->
            dynamicTest(index.toString()) {
                val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
                    list,
                    { this },
                    episodeTitles = listOf("未来日记"),
                    episodeSort = EpisodeSort("SP"),
                    episodeEp = null,
                )
                assertEquals(
                    "[DBD-Raws][未来日记][SP][1080P][BDRip][HEVC-10bit][FLAC].mkv",
                    selected,
                    message = list.toString(),
                )
            }
        },
    )

    @TestFactory
    fun `select SP 01 over PV`() = runDynamicTests(
        listOf(
            "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
            "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][SP][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        ).permuted().mapIndexed { index, list ->
            dynamicTest(index.toString()) {
                val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
                    list,
                    { this },
                    episodeTitles = listOf("未来日记"),
                    episodeSort = EpisodeSort("SP"),
                    episodeEp = null,
                )
                assertEquals(
                    "[DBD-Raws][未来日记][SP][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
                    selected,
                    message = list.toString(),
                )
            }
        },
    )

    @TestFactory
    fun `select OVA over PV`() = runDynamicTests(
        listOf(
            "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
            "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][OVA][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        ).permuted().mapIndexed { index, list ->
            dynamicTest(index.toString()) {
                val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
                    list,
                    { this },
                    episodeTitles = listOf("未来日记"),
                    episodeSort = EpisodeSort("OVA"),
                    episodeEp = null,
                )
                assertEquals(
                    "[DBD-Raws][未来日记][OVA][1080P][BDRip][HEVC-10bit][FLAC].mkv",
                    selected,
                    message = list.toString(),
                )
            }
        },
    )

    @TestFactory
    fun `select OVA12 over PV`() = runDynamicTests(
        listOf(
            "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
            "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][OVA12][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        ).permuted().mapIndexed { index, list ->
            dynamicTest(index.toString()) {
                val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
                    list,
                    { this },
                    episodeTitles = listOf("未来日记"),
                    episodeSort = EpisodeSort("OVA"),
                    episodeEp = null,
                )
                assertEquals(
                    "[DBD-Raws][未来日记][OVA12][1080P][BDRip][HEVC-10bit][FLAC].mkv",
                    selected,
                    message = list.toString(),
                )
            }
        },
    )

    @TestFactory
    fun `select normal 01 over PV if no match`() = runDynamicTests(
        listOf(
            "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
            "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][SP][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
            "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        ).permuted().mapIndexed { index, list ->
            dynamicTest(index.toString()) {
                val selected = TorrentVideoSourceResolver.selectVideoFileEntry(
                    list,
                    { this },
                    episodeTitles = listOf("未来日记"),
                    episodeSort = EpisodeSort("08"),
                    episodeEp = null,
                )
                assertEquals(
                    "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
                    selected,
                    message = list.toString(),
                )
            }
        },
    )
}
