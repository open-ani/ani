package me.him188.ani.datasources.bangumi

import me.him188.ani.datasources.api.paging.AbstractPageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.subject.Subject
import me.him188.ani.datasources.api.subject.SubjectImages
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import me.him188.ani.datasources.api.subject.SubjectType
import me.him188.ani.datasources.bangumi.models.subjects.BangumiLegacySubject
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.subjects.toSubject

class BangumiPagedSource(
    private val client: BangumiClient,
    private val query: SubjectSearchQuery,
    private val pageSize: Int = 25,
) : AbstractPageBasedPagedSource<Subject>() {

    override suspend fun nextPageImpl(page: Int): List<Subject> {
        val paged: Paged<Subject>;
        if (query.useOldSearchApi) {
            val tmpPaged = client.subjects.searchSubjectsByKeywordsWithOldApi(
                query.keyword,
                convertType(),
                null,
                page * pageSize,
                pageSize
            )
            paged = Paged(
                total = tmpPaged.total,
                hasMore = tmpPaged.hasMore,
                page = tmpPaged
                    .page.map { convert2Subject(it) }
            )
        } else {
            val tmpPaged = client.subjects.searchSubjectByKeywords(
                query.keyword,
                offset = page * pageSize,
                // 才有 rating
                limit = pageSize,
                types = listOf(convertType()),
            )
            paged = Paged(
                total = tmpPaged.total,
                hasMore = tmpPaged.hasMore,
                page = tmpPaged
                    .page.map { it.toSubject() }
            )
        }

        if (!paged.hasMore) {
            noMorePages()
        }
        return paged.page
    }

    private fun convert2Subject(legaSub: BangumiLegacySubject): Subject {
        return Subject(
            id = legaSub.id,
            originalName = legaSub.originalName,
            chineseName = legaSub.chineseName,
            score = 0.0,
            rank = legaSub.rank.let { 0 },
            tags = listOf(),
            sourceUrl = legaSub.url.let { "" },
            images = SubjectImages(
                landscapeCommon = BangumiClientImpl.getSubjectImageUrl(
                    legaSub.id,
                    BangumiSubjectImageSize.MEDIUM
                ),
                largePoster = BangumiClientImpl.getSubjectImageUrl(
                    legaSub.id,
                    BangumiSubjectImageSize.LARGE
                ),
            ),
            summary = legaSub.summary,
        )
    }

    private fun convertType() = when (query.type) {
        SubjectType.ANIME -> BangumiSubjectType.ANIME
    }

}