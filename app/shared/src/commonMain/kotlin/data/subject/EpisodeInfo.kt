package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort
import org.openapitools.client.models.Episode

/**
 * 与数据源无关的条目信息.
 *
 * @see Episode
 */
@Immutable
@Serializable
data class EpisodeInfo(
    val id: Int,
    /** `0` 本篇，`1` SP，`2` OP，`3` ED */
    val type: EpisodeType = EpisodeType.MainStory,
    val name: String = "",
    val nameCn: String = "",
    val airDate: PackedDate = PackedDate.Invalid,
    val comment: Int = 0,
    /** 维基人填写的原始时长 */
    val duration: String = "",
    /** 简介 */
    val desc: String = "",
    /** 音乐曲目的碟片数 */
    val disc: Int = 0,
    /** 同类条目的排序和集数 */
    val sort: EpisodeSort = EpisodeSort(""),
    /** 条目内的集数, 从`1`开始。非本篇剧集的此字段无意义 */
    val ep: EpisodeSort? = null,
//    /** 服务器解析的时长，无法解析时为 `0` */
//    val durationSeconds: Int? = null
)

@Stable
val EpisodeInfo.displayName get() = nameCnOrName

@Stable
val EpisodeInfo.nameCnOrName: String
    get() = nameCn.ifBlank { name }

/**
 * 是否一定已经播出了
 */
@Stable
val EpisodeInfo.isKnownBroadcast: Boolean
    get() = airDate.isValid && airDate <= PackedDate.now() // TODO: consider time 

/**
 * 是否一定还未播出
 */
@Stable
val EpisodeInfo.isKnownOnAir
    get() = airDate.isValid && airDate > PackedDate.now() // TODO: consider time 

@Stable
fun EpisodeInfo.renderEpisodeEp() = when (type) {
    EpisodeType.MainStory -> { // 本篇
        sort.toString()
    } // "01", "12", "26", "120"

    EpisodeType.SP -> { // SP
        "SP$sort"
    } // "SP", "SP1", "SP10"

    else -> {
        "PV$sort"
    } // "PV", "PV1", "PV10"
}

@JvmInline
@Serializable
@Immutable
value class EpisodeType(val value: Int) {
    companion object {
        @Stable
        val MainStory = EpisodeType(0)

        @Stable
        val SP = EpisodeType(1)

        @Stable
        val OP = EpisodeType(2)

        @Stable
        val ED = EpisodeType(3)
    }
}