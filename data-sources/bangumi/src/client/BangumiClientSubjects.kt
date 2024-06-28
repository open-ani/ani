package me.him188.ani.datasources.bangumi.client

import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.bangumi.models.search.BangumiSort
import me.him188.ani.datasources.bangumi.models.subjects.BangumiLegacySubject
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubject
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectDetails
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectType
import org.openapitools.client.models.RelatedPerson

interface BangumiClientSubjects {
    /**
     * 搜索条目.
     *
     * @param keyword 关键字, 例如番剧名称
     * @param types 条目类型, 默认为 [BangumiSubjectType.ANIME]
     * @param responseGroup 返回数据大小, 默认为 [BangumiResponseGroup.SMALL]
     * @param offset 开始条数, 默认为 0
     * @param limit 返回条数, 最大为 25
     * @return 搜索结果, null 表示已经到达最后一条
     */
    suspend fun searchSubjectByKeywords(
        keyword: String,
        offset: Int? = null,
        limit: Int? = null,
        sort: BangumiSort? = null,
        types: List<BangumiSubjectType>? = null,
        tags: List<String>? = null, // "童年", "原创"
        airDates: List<String>? = null, // YYYY-MM-DD
        ratings: List<String>? = null, // ">=6", "<8"
        ranks: List<String>? = null,
        nsfw: Boolean? = null,
    ): Paged<BangumiSubject>

    /**
     * Search bangumi subjects by old api.
     *
     * @see <https://github.com/bangumi/server>
     * @see <https://bangumi.github.io/api/>
     *
     * @param keyword subject keyword, such as name.
     * @param type bangumi subject type, default search all types when it is null.
     * @param responseGroup return the data size which default is small, available values : [small, medium, large], default value is small.
     * @param start start page, default is first page.
     * @param maxResults max results in page, max is 25.
     * @see BangumiSubjectType
     * @see BangumiSubjectImageSize
     *
     */
    suspend fun searchSubjectsByKeywordsWithOldApi(
        keyword: String,
        type: BangumiSubjectType?,
        responseGroup: BangumiSubjectImageSize?,
        start: Int?,
        maxResults: Int?
    ): Paged<BangumiLegacySubject>

    suspend fun getSubjectById(
        id: Int,
    ): BangumiSubjectDetails?

    fun getSubjectImageUrl(
        id: Int,
        size: BangumiSubjectImageSize,
    ): String

    suspend fun getSubjectPersonsById(
        id: Int,
    ): List<RelatedPerson>?
}