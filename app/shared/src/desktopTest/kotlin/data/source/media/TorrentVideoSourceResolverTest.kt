package me.him188.ani.app.data.source.media

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.him188.ani.app.data.source.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.test.permuted
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
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
    fun `select normal over PV`() = listOf(
        "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
        "[DBD-Raws][未来日记] PV [01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记] PV01 [1080P][BDRip][HEVC-10bit][FLAC].mkv",
    ).permuted().mapIndexed { index, list ->
        DynamicTest.dynamicTest(index.toString()) {
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
    }

    @TestFactory
    fun `select SP over PV`() = listOf(
        "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
        "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][SP][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
    ).permuted().mapIndexed { index, list ->
        DynamicTest.dynamicTest(index.toString()) {
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
    }

    @TestFactory
    fun `select SP 01 over PV`() = listOf(
        "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
        "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][SP][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
    ).permuted().mapIndexed { index, list ->
        DynamicTest.dynamicTest(index.toString()) {
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
    }

    @TestFactory
    fun `select OVA over PV`() = listOf(
        "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
        "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][OVA][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
    ).permuted().mapIndexed { index, list ->
        DynamicTest.dynamicTest(index.toString()) {
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
    }

    @TestFactory
    fun `select OVA12 over PV`() = listOf(
        "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
        "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][OVA12][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
    ).permuted().mapIndexed { index, list ->
        DynamicTest.dynamicTest(index.toString()) {
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
    }

    @TestFactory
    fun `select normal 01 over PV if no match`() = listOf(
        "[DBD-Raws][未来日记][01][1080P][BDRip][HEVC-10bit][FLACx2].mkv",
        "[DBD-Raws][未来日记][PV][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][SP][01][1080P][BDRip][HEVC-10bit][FLAC].mkv",
        "[DBD-Raws][未来日记][NCOP1][1080P][BDRip][HEVC-10bit][FLAC].mkv",
    ).permuted().mapIndexed { index, list ->
        DynamicTest.dynamicTest(index.toString()) {
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
    }

}
