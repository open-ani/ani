package me.him188.ani.datasources.api

import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.EpisodeType.MAD
import me.him188.ani.datasources.api.EpisodeType.OAD
import me.him188.ani.datasources.api.EpisodeType.OVA
import me.him188.ani.datasources.api.EpisodeType.PV
import me.him188.ani.datasources.api.EpisodeType.SP
import me.him188.ani.datasources.api.EpisodeType.Unknown
import me.him188.ani.test.DynamicTestsResult
import me.him188.ani.test.TestContainer
import me.him188.ani.test.TestFactory
import me.him188.ani.test.dynamicTest
import me.him188.ani.test.permutedSequence
import me.him188.ani.test.runDynamicTests
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import me.him188.ani.datasources.api.EpisodeSort as ep
import me.him188.ani.datasources.api.EpisodeSort.Normal as normal
import me.him188.ani.datasources.api.EpisodeSort.Special as special

@TestContainer
class EpisodeSortTest {
    @TestFactory
    fun `parse string`(): DynamicTestsResult {
        return runDynamicTests(
            listOf(
                // Actual to Expected
                "1" to normal(1f),
                "0" to normal(0f),
                "01" to normal(1f),
                "1.0" to normal(1f),
                "1.5" to normal(1.5f),
                "2.5" to normal(2.5f),
                "0.5" to normal(0.5f),
                "05.5" to normal(5.5f),
                "30.5" to normal(30.5f),
                "-1" to special(Unknown, -1f),
                "1.1" to EpisodeSort.Unknown("1.1"),
                "SP1" to special(SP, 1f),
                "SP02" to special(SP, 2f),
                "SP1.5" to special(SP, 1.5f),
                "MAD1" to special(MAD, 1f),
                "MAD02" to special(MAD, 2f),
                "OVA04" to special(OVA, 4f),
                "OAD08" to special(OAD, 8f),
                "S" to EpisodeSort.Unknown("S"),
            ).map {
                dynamicTest(it.first) {
                    val (raw, expected) = it
                    val actual = ep(raw)
                    assertEquals(expected, actual)
                    assertEquals(expected.hashCode(), actual.hashCode())
                }
            },
        )
    }

    @TestFactory
    fun `to string`(): DynamicTestsResult {
        return runDynamicTests(
            listOf(
                // Actual to Expected
                normal(1f) to "01",
                normal(0f) to "00",
                normal(1f) to "01",
                normal(1.5f) to "1.5",
                normal(2.5f) to "2.5",
                normal(0.5f) to "0.5",
                normal(5.5f) to "5.5",
                normal(30.5f) to "30.5",
                special(SP, -1f) to "SP-1",
                special(MAD, 1.5f) to "MAD1.5",
                special(Unknown, -999f) to "Unknown-999",
                EpisodeSort.Unknown("S") to "S",
            ).map {
                dynamicTest(it.second) {
                    val (raw, expected) = it
                    val actual = raw.toString()
                    assertEquals(expected, actual)
                }
            },
        )
    }

    @TestFactory
    fun equals() = runDynamicTests(
        listOf(
            normal(1f) to normal(1f),
            normal(1.5f) to normal(1.5f),
            normal(0f) to normal(0f),
            special(SP, 1f) to special(SP, 1f),
            special(MAD, 2f) to special(MAD, 2f),
            EpisodeSort.Unknown("S") to EpisodeSort.Unknown("S"),
        ).map {
            dynamicTest(it.toString()) {
                val (a, b) = it
                assertEquals(a, b)
                assertEquals(b, a)
                assertEquals(b.hashCode(), a.hashCode())
            }
        },
    )

    @TestFactory
    fun `not equals`() = runDynamicTests(
        listOf(
            normal(1f) to normal(2f),
            normal(1f) to special(SP, 1f),
            special(PV, 1.5f) to special(PV, 1f),
            special(MAD, 1.0f) to special(PV, 1f),
            EpisodeSort.Unknown("1 ") to EpisodeSort.Unknown("1"),
        ).map {
            dynamicTest(it.toString()) {
                val (a, b) = it
                assertNotEquals(a, b)
                assertNotEquals(b, a)
                assertNotEquals(b.hashCode(), a.hashCode()) // our extra constraint
            }
        },
    )

    @TestFactory
    fun compare() = runDynamicTests(
        listOf(
            // Original to Sorted Ascending
            "int" to listOf(ep(1), ep(2), ep(3)),
            "float" to listOf(ep(1), ep("1.5"), ep("2"), ep(3)),
            "sp" to listOf(ep("SP1"), ep("SP2")),
            "int with sp" to listOf(ep(1), ep(3), ep("SP1")),
            "float with sp" to listOf(ep("1.5"), ep(3), ep("SP1")),
        ).flatMap { (name, sorted) ->
            sorted.permutedSequence().map { unsorted ->
                dynamicTest("$name: $unsorted => $sorted") {
                    assertEquals(sorted, unsorted.sorted())
                }
            }
        },
    )

    @TestFactory
    fun toFloats() = runDynamicTests(
        listOf(
            2f to "02".toFloat(),
            2f to "2".toFloat(),
        ).map {
            dynamicTest(it.toString()) {
                val (a, b) = it
                assertEquals(a, b)
            }
        },
    )
    
}
