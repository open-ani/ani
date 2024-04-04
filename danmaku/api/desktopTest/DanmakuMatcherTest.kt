package me.him188.ani.danmaku.api

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DanmakuMatcherTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val testData = json.decodeFromString(
        ListSerializer(DanmakuEpisode.serializer()),
        """
        [{"id":"175640013","subjectName":"迷宫饭","episodeName":"第13话 炎龙3/良药"},{"id":"181620013","subjectName":"地下城里的人们","episodeName":"第13话"},{"id":"108940013","subjectName":"在地下城寻求邂逅是否搞错了什么","episodeName":"第13话 眷族物语(Familiar Myth)"},{"id":"140890013","subjectName":"饭沼。","episodeName":"第13话 ハンバーガー食べよう... 前編"},{"id":"86900001","subjectName":"ToHeart2 迷宫旅人","episodeName":"第1话 最坏的灾难"},{"id":"86900002","subjectName":"ToHeart2 迷宫旅人","episodeName":"第2话 たいせつなもの"},{"id":"145590013","subjectName":"阿拉德：逆转之轮","episodeName":"第13话 希望"},{"id":"119410001","subjectName":"在地下城寻求邂逅是否搞错了什么 OVA","episodeName":"第1话 在地下城寻求温泉是否搞错了什么"},{"id":"27690001","subjectName":"1月にはChristmas","episodeName":"第1话 OVA"},{"id":"27130003","subjectName":"奇兵大冒险 风之塔","episodeName":"第3话 おまたせコーダ"},{"id":"134610013","subjectName":"猪猪侠 超星萌宠","episodeName":"第13话 穿越阿五的回忆"},{"id":"27130001","subjectName":"奇兵大冒险 风之塔","episodeName":"第1话 よろしくプレリュード"},{"id":"27130002","subjectName":"奇兵大冒险 风之塔","episodeName":"第2话 まだまだカプリッチオ"},{"id":"166930013","subjectName":"如果历史是一群喵","episodeName":"第13话"},{"id":"4340013","subjectName":"中华小当家","episodeName":"第13话 鲶鱼面完成！命运的审判"},{"id":"23620013","subjectName":"银河飘流华尔分13","episodeName":"第13话 絶体絶命! さらば愛しきJr.たち"},{"id":"57460013","subjectName":"骷髅13","episodeName":"第13话 交叉的夹角"},{"id":"18790001","subjectName":"云界的迷宫","episodeName":"第1话 Volume 1"},{"id":"18790002","subjectName":"云界的迷宫","episodeName":"第2话 Volume 2"},{"id":"69820001","subjectName":"町一番のけちんぼう","episodeName":"第1话 TV Special"},{"id":"82670001","subjectName":"寒月一凍悪霊斬り","episodeName":"第1话 Volume 1"},{"id":"82670002","subjectName":"寒月一凍悪霊斬り","episodeName":"第2话 Volume 2"},{"id":"127970001","subjectName":"吸血鬼仆人 Alice in the Garden","episodeName":"剧场版"},{"id":"10760001","subjectName":"骷髅13 剧场版","episodeName":"第1话 骷髅13 剧场版"},{"id":"10770001","subjectName":"骷髅13 女王蜂","episodeName":"第1话 OVA"}]
    """.trimIndent()
    ).shuffled(Random(123))

    @Test
    fun testMatch() {
        assertEquals(
            "175640013",
            DanmakuMatchers.mostRelevant("迷宫饭", "第13话 炎龙3").match(testData)?.id
        )
    }

    @Test
    fun testEmpty() {
        assertEquals(
            null,
            DanmakuMatchers.mostRelevant("迷宫饭", "第13话 炎龙3").match(emptyList())
        )
    }
}