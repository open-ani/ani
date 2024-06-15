package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import me.him188.ani.datasources.api.EpisodeSort
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeDetail
import java.math.BigDecimal

/**
 * Unified from [Episode] and [EpisodeDetail].
 *
 * @see Episode
 */
@Immutable
class EpisodeInfo(
    val id: Int,
    /** `0` 本篇，`1` SP，`2` OP，`3` ED */
    val type: EpisodeType = EpisodeType.MainStory,
    val name: String = "",
    val nameCn: String = "",
    val airdate: PackedDate = PackedDate.Invalid,
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
    val ep: EpisodeSort = EpisodeSort(""),
//    /** 服务器解析的时长，无法解析时为 `0` */
//    val durationSeconds: Int? = null
) {
    val nameCnOrName: String
        get() = nameCn.ifBlank { name }
}

fun Episode.toEpisodeInfo(): EpisodeInfo {
    return EpisodeInfo(
        id = this.id,
        type = EpisodeType(this.type),
        name = this.name,
        nameCn = this.nameCn,
        airdate = PackedDate.parseFromDate(this.airdate),
        comment = this.comment,
        duration = this.duration,
        desc = this.desc,
        disc = this.disc,
        sort = EpisodeSort(this.sort),
        ep = EpisodeSort(this.ep ?: BigDecimal.ONE),
//        durationSeconds = this.durationSeconds
    )
}

fun EpisodeDetail.toEpisodeInfo(): EpisodeInfo {
    return EpisodeInfo(
        id = id,
        type = EpisodeType(this.type),
        name = name,
        nameCn = nameCn,
        sort = EpisodeSort(this.sort),
        airdate = PackedDate.parseFromDate(this.airdate),
        comment = comment,
        duration = duration,
        desc = desc,
        disc = disc,
        ep = EpisodeSort(this.ep ?: BigDecimal.ONE),
    )
}

@JvmInline
value class EpisodeType(val value: Int) {
    companion object {
        val MainStory = EpisodeType(0)
        val SP = EpisodeType(1)
        val OP = EpisodeType(2)
        val ED = EpisodeType(3)
    }
}