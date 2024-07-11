package me.him188.ani.danmaku.api

import kotlinx.coroutines.test.runTest
import me.him188.ani.danmaku.ui.DanmakuFilterConfig
import me.him188.ani.danmaku.ui.DanmakuRegexFilter
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class FilterListTest {

    private fun filterList(
        list: List<Danmaku>,
        danmakuFilterConfig: DanmakuFilterConfig,
        danmakuRegexFilterEnabled: Boolean
    ): List<Danmaku> {
        if (!danmakuRegexFilterEnabled) {
            return list
        }

        // 预编译所有启用的正则表达式 
        val regexFilters = danmakuFilterConfig.danmakuRegexFilterList
            .filter { it.enabled }
            .map { Regex(it.regex) }

        return list.filter { danmaku ->
            !regexFilters.any { regex ->
                danmaku.text.matches(regex)
            }
        }
    }

    private fun dummyDanmaku(timeSeconds: Double, text: String = "$timeSeconds") =
        Danmaku(text, "dummy", (timeSeconds * 1000L).toLong(), text, DanmakuLocation.NORMAL, text, 0)

    private fun create(
        sequence: Sequence<Danmaku>,
        repopulateThreshold: Duration = 3.seconds,
        repopulateDistance: Duration = 2.seconds,
    ): DanmakuSessionAlgorithm =
        DanmakuSessionAlgorithm(
            DanmakuSessionFlowState(sequence.toList(),
                repopulateThreshold = repopulateThreshold,
                repopulateDistance = { repopulateDistance })
        )

    @Test
    fun `filter no danmaku`() = runTest {
        val danmakuFilterList = listOf<DanmakuRegexFilter>(
            DanmakuRegexFilter(UUID.randomUUID().toString(), name = "1", regex = ".*簽.*", enabled = true),
            DanmakuRegexFilter(UUID.randomUUID().toString(), name = "2", regex = "2", enabled = true),
            DanmakuRegexFilter(UUID.randomUUID().toString(), name = "3", regex = "3", enabled = true),
        )
        val danmakuFilterConfig =
            DanmakuFilterConfig(danmakuRegexFilterList = danmakuFilterList)
        val danmakuList = listOf(
            dummyDanmaku(1.0, "簽到"),
        )
        val danmakuRegexFilterEnabled = true
        val list = filterList(danmakuList, danmakuFilterConfig, danmakuRegexFilterEnabled)
        assertEquals(0, list.size)
    }
}