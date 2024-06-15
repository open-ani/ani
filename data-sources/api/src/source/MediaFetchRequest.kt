package me.him188.ani.datasources.api.source

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort

/**
 * 一个数据源查询请求. 该请求包含尽可能多的信息以便 [MediaSource] 可以查到尽可能多的结果.
 *
 * @see MediaSource.fetch
 */
@Serializable
class MediaFetchRequest(
    /**
     * 条目服务 (Bangumi) 提供的条目 ID. 若数据源支持, 可以用此信息做精确匹配.
     * 可能为 `null`, 表示未知.
     */
    val subjectId: String? = null,
    /**
     * 条目服务 (Bangumi) 提供的剧集 ID. 若数据源支持, 可以用此信息做精确匹配.
     * 可能为 `null`, 表示未知.
     */
    val episodeId: String? = null,
    /**
     * 条目的主简体中文名称.
     * 建议使用 [subjectNames] 用所有已知名称去匹配.
     */
    val subjectNameCN: String? = null,
    /**
     * 已知的该条目的所有名称. 包含季度信息.
     *
     * 所有名称包括简体中文译名, 各种别名, 简称, 以及日文原名.
     *
     * E.g. "关于我转生变成史莱姆这档事 第三季"
     */
    val subjectNames: Set<String>,
    /**
     * 在系列中的集数, 例如第二季的第一集为 26.
     *
     * E.g. "49", "01".
     *
     * @see EpisodeSort
     */
    val episodeSort: EpisodeSort,
    /**
     * 条目服务 (Bangumi) 提供的剧集名称, 例如 "恶魔与阴谋", 不会包含 "第 x 集".
     * 不一定为简体中文, 可能为日文. 也可能为空字符串.
     */
    val episodeName: String,
    /**
     * 在当前季度中的集数, 例如第二季的第一集为 01
     *
     * E.g. "49", "01".
     *
     * @see EpisodeSort
     */
    val episodeEp: EpisodeSort? = episodeSort,
)
