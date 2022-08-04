package me.him188.animationgarden.api.tags

import me.him188.animationgarden.api.model.TopicDetails

@JvmInline
value class Episode(
    val raw: String,
) : Comparable<Episode> {
    override fun compareTo(other: Episode): Int {
        return this.raw.compareTo(other.raw) // TODO: 2022/8/4 check episode compare
    }
}

data class Tag(
    val id: String,
)

enum class MediaOrigin(
    val id: String,
    vararg val otherNames: String,
) {
    BDRip("BDRip"),
    BluRay("Blu-Ray", "BluRay"),
    WebRip("WebRip"), ;

    companion object {
        private val values by lazy { values() }
        fun tryParse(text: String): MediaOrigin? {
            for (value in values) {
                if (text.contains(value.id, ignoreCase = true)
                    || value.otherNames.any { text.contains(it, ignoreCase = true) }
                ) {
                    return value
                }
            }
            return null
        }
    }
}

sealed class SubtitleLanguage(
    val id: String,
) {
    abstract fun matches(text: String): Boolean
    override fun toString(): String {
        return id
    }

    sealed class Chinese(id: String) : SubtitleLanguage(id)

    object ChineseSimplified : Chinese("CHS") {
        private val tokens =
            arrayOf("简中", "GB", "GBK", "简体中文", "中文", "中字", "简", "CHS", "Zh-Hans", "Zh_Hans", "zh_cn")

        override fun matches(text: String): Boolean {
            return tokens.any { text.contains(it, ignoreCase = true) }
        }
    }

    object ChineseTraditional : Chinese("CHT") {
        private val tokens = arrayOf("繁中", "BIG5", "BIG 5", "繁", "Chinese", "CHT")
        override fun matches(text: String): Boolean {
            return tokens.any { text.contains(it, ignoreCase = true) }
        }

    }

    object Japanese : SubtitleLanguage("JPN") {
        private val tokens = arrayOf("日", "Japanese")
        override fun matches(text: String): Boolean {
            return tokens.any { text.contains(it, ignoreCase = true) }
        }
    }

    object English : SubtitleLanguage("ENG") {
        private val tokens = arrayOf("英", "English")
        override fun matches(text: String): Boolean {
            return tokens.any { text.contains(it, ignoreCase = true) }
        }
    }

    companion object {
        val entries = arrayOf(ChineseSimplified, ChineseTraditional, Japanese, English)
    }
}

sealed class Resolution(
    val id: String,
    private vararg val otherNames: String,
) {
    override fun toString(): String {
        return id
    }

    object R240P : Resolution("240P", "x240")
    object R360P : Resolution("360P", "x360")
    object R480P : Resolution("480P", "x480")
    object R560P : Resolution("560P", "x560")
    object R720P : Resolution("720P", "x720")
    object R1080P : Resolution("1080P", "x1080")
    object R1440P : Resolution("1440P", "x1440")
    object R2160P : Resolution("2160P", "x2160")

    companion object {
        val entries = arrayOf(
            R240P, R360P, R480P, R560P, R720P, R1080P, R1440P, R2160P,
        )

        fun tryParse(text: String): Resolution? {
            for (entry in entries) {
                if (text.contains(entry.id, ignoreCase = true)
                    || entry.otherNames.any { text.contains(it, ignoreCase = true) }
                ) {
                    return entry
                }
            }
            return null
        }
    }
}

data class FrameRate(
    val value: Int,
) {
    companion object {
        val F60 = FrameRate(60)

        fun tryParse(text: String): FrameRate? {
            // TODO: 2022/8/4 optimize
            if (text.contains("@60")) {
                return F60
            }
            if (text.contains("1080P60")) {
                return F60
            }
            if (text.contains("2160P60")) {
                return F60
            }
            if (text.contains("60FPS")) {
                return F60
            }
            if (text.contains("60 FPS")) {
                return F60
            }
            return null
        }
    }
}

class RawTitleParserA : RawTitleParser() {
    override fun parse(
        text: String,
        allianceName: String?,
        collectTag: (title: String) -> Unit,
        collectChineseTitle: (String) -> Unit,
        collectOtherTitle: (String) -> Unit,
        collectEpisode: (Episode) -> Unit,
        collectResolution: (Resolution) -> Unit,
        collectFrameRate: (FrameRate) -> Unit,
        collectMediaOrigin: (MediaOrigin) -> Unit,
        collectSubtitleLanguage: (SubtitleLanguage) -> Unit
    ) {
        val exceptTagsBuilder = StringBuilder()
        var index = 0
        for (result in brackets.findAll(text)) {
            if (index < result.range.first) {
                exceptTagsBuilder.append(text.subSequence(index until result.range.first))
            }
            index = result.range.last + 1

            val tagOrTags = result.groups[1]!!.value // can be "WebRip 1080p HEVC-10bit AAC" or "简繁内封字幕"
            for (tag in tagOrTags.splitToSequence(' ')) {
                processTag(
                    allianceName = allianceName,
                    tag = tag,
                    collectSubtitleLanguage = collectSubtitleLanguage,
                    collectResolution = collectResolution,
                    collectFrameRate = collectFrameRate,
                    collectMediaOrigin = collectMediaOrigin,
                    collectEpisode = collectEpisode,
                    collectTag = collectTag
                )
            }
        }
        if (index < text.length) {
            exceptTagsBuilder.append(text.subSequence(index until text.length))
        }

        val exceptTags = exceptTagsBuilder.toString()

        // [喵萌奶茶屋&LoliHouse] 继母的拖油瓶是我的前女友 / Mamahaha no Tsurego ga Motokano datta - 04 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]
        // Lilith-Raws [Lilith-Raws] Overlord IV - 05 [Baha][WEB-DL][1080p][AVC AAC][CHT][MP4] 約1條評論
        // NC-Raws [NC-Raws] OVERLORD IV / Overlord S4 - 05 (B-Global 3840x2160 HEVC AAC MKV)
        // 桜都字幕组 [桜都字幕组] RWBY 冰雪帝国 / RWBY Hyousetsu Teikoku [05][1080p][简繁内封] 約1條評論
        // 天月動漫&發佈組 [Skymoon-Raws] 新網球王子: U-17 WORLD CUP / Shin Tennis no Ouji-sama: U-17 World Cup - 05 [ViuTV][WEB-RIP][720p][HEVC AAC][CHT][MP4]
        // 天月動漫&發佈組 [天月搬運組] 異世界迷宮裡的後宮生活 / Isekai Meikyuu de Harem wo - 05 [1080P][簡繁日外掛] 約1條評論
        // LoliHouse [喵萌奶茶屋&LoliHouse] 风都侦探 / Fuuto Tantei / FUUTO PI - 01 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕] 約1條評論
        // LoliHouse [LoliHouse] 邪神与厨二病少女X / Jashin-chan Dropkick X - 05 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕] 約1條評論
        // LoliHouse [喵萌Production&LoliHouse] LoveLive! 超级明星!! 2期 / Love Live! Superstar!! S2 - 03 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕] 約1條評論


        // 幻月字幕组 【幻月字幕组】【22年日剧】【恶女的一切】【05】【1080P】【中日双语】
        // 魔星字幕团 [MagicStar] ANIMALS / アニマルズ EP07 [WEBDL] [1080p] [ABEMA]【生】
        // 魔星字幕团 【合集】[MagicStar] 各自的断崖 / それぞれの断崖 [WEBDL] [1080p] [HULU]【生】

        // 悠哈C9字幕社 【悠哈璃羽字幕社】[最近僱的女僕有點奇怪_Saikin Yatotta Maid ga Ayashii][02][x264 1080p][CHT]
        // 极影字幕社 【极影字幕社】 ★4月新番 【间谍过家家】【SPY×FAMILY】【10】GB MP4_720P
        // 喵萌奶茶屋 【喵萌奶茶屋】★07月新番★[契约之吻/Engage Kiss][05][1080p][简体][招募翻译校对] 約2條評論

        exceptTags.substringAfterLast('-', "").takeIf { it.isNotBlank() }?.trim()?.let {
            collectEpisode(Episode(it))
        }

        val names = exceptTags.substringBeforeLast('-')
        names.substringBefore('/').trim().let(collectChineseTitle)
        names.substringAfter('/').trim().let(collectOtherTitle)
    }

    private fun processTag(
        allianceName: String?,
        tag: String,
        collectSubtitleLanguage: (SubtitleLanguage) -> Unit,
        collectResolution: (Resolution) -> Unit,
        collectFrameRate: (FrameRate) -> Unit,
        collectMediaOrigin: (MediaOrigin) -> Unit,
        collectEpisode: (Episode) -> Unit,
        collectTag: (name: String) -> Unit,
    ) {
        if (allianceName != null && tag.contains(allianceName)) return

        var anyMatched = false
        anyMatched = anyMatched or tag.parseSubtitleLanguages(collectSubtitleLanguage)
        anyMatched = anyMatched or tag.parseResolution(collectResolution)
        anyMatched = anyMatched or tag.parseFrameRate(collectFrameRate)
        anyMatched = anyMatched or tag.parseMediaOrigin(collectMediaOrigin)
        anyMatched = anyMatched or tag.parseEpisode(collectEpisode)

        if (!anyMatched) {
            collectTag(tag)
        }
    }
}

fun RawTitleParser.parse(text: String, allianceName: String?, builder: TopicDetails.Builder) {
    return parse(
        text, allianceName,
        collectTag = { builder.tags.add(it) },
        collectChineseTitle = { builder.chineseTitle = it },
        collectOtherTitle = { builder.otherTitle = it },
        collectEpisode = { builder.episode = it },
        collectResolution = { builder.resolution = it },
        collectFrameRate = { builder.frameRate = it },
        collectMediaOrigin = { builder.mediaOrigin = it },
        collectSubtitleLanguage = { builder.subtitleLanguages.add(it) }
    )
}

abstract class RawTitleParser {
    // 【极影字幕社】 ★7月新番 【来自深渊 烈日的黄金乡】【Made in Abyss - Retsujitsu no Ougonkyou】【04】GB MP4_1080P
    // [獸耳娘噠萌進化][第352期][500P]
    // [猎户不鸽发布组] 比赛开始,零比零 / 羽球青春 Love All Play [16-17] [1080p] [简中] [网盘] [2022年4月番]
    // [喵萌奶茶屋&LoliHouse] 继母的拖油瓶是我的前女友 / Mamahaha no Tsurego ga Motokano datta - 04 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]

    protected val brackets = Regex("""[\[【(](.*?)[]】)]""")

    abstract fun parse(
        text: String,
        allianceName: String?, // used to filter out alliance tags.
        collectTag: (title: String) -> Unit,
        collectChineseTitle: (String) -> Unit,
        collectOtherTitle: (String) -> Unit,
        collectEpisode: (Episode) -> Unit, // may be 12.5 or SP1
        collectResolution: (Resolution) -> Unit,
        collectFrameRate: (FrameRate) -> Unit,
        collectMediaOrigin: (MediaOrigin) -> Unit, // WebRip BDRip Blu-ray
        collectSubtitleLanguage: (SubtitleLanguage) -> Unit, // may be called multiple times
    )

    protected fun String.parseSubtitleLanguages(collect: (SubtitleLanguage) -> Unit): Boolean {
        var any = false
        for (entry in SubtitleLanguage.entries) {
            if (entry.matches(this)) {
                collect(entry)
                any = true
            }
        }
        return any
    }

    protected fun String.parseResolution(collect: (Resolution) -> Unit): Boolean {
        return Resolution.tryParse(this)?.let(collect) != null
    }

    protected fun String.parseFrameRate(collect: (FrameRate) -> Unit): Boolean {
        return FrameRate.tryParse(this)?.let(collect) != null
    }

    protected fun String.parseMediaOrigin(collect: (MediaOrigin) -> Unit): Boolean {
        return MediaOrigin.tryParse(this)?.let(collect) != null
    }

    protected fun String.parseEpisode(collectEpisode: (Episode) -> Unit): Boolean {
        this.toFloatOrNull()?.let {
            collectEpisode(Episode(this))
            return true
        }
        if (this.contains("SP", ignoreCase = true) || this.contains("小剧场")) {
            collectEpisode(Episode(this))
            return true
        }
        return false
    }


    companion object {
        fun getParserFor(allianceName: String?): RawTitleParser? {
            when (allianceName) {
                "LoliHouse",
                "NC-RAWS",
                "Lilith-Raws",
                "桜都字幕组",
                "Skymoon-Raws",
                "天月動漫&發佈組",
                "天月搬運組",
                -> {
                    return RawTitleParserA()
                }

                else -> {}
            }
            return null
        }
    }
}