package me.him188.ani.datasources.bangumi

import me.him188.ani.datasources.api.paging.AbstractPageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.subject.Subject
import me.him188.ani.datasources.api.subject.SubjectImages
import me.him188.ani.datasources.api.subject.SubjectSearchQuery
import me.him188.ani.datasources.api.subject.SubjectType
import me.him188.ani.datasources.bangumi.models.BangumiSearchSubjects200ResponseDataInner
import me.him188.ani.datasources.bangumi.models.BangumiSearchSubjectsRequest
import me.him188.ani.datasources.bangumi.models.BangumiSearchSubjectsRequest.Sort
import me.him188.ani.datasources.bangumi.models.BangumiSearchSubjectsRequestFilter
import me.him188.ani.datasources.bangumi.models.BangumiSubject
import me.him188.ani.datasources.bangumi.models.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.subjects.BangumiLegacySubject
import me.him188.ani.datasources.bangumi.models.subjects.BangumiSubjectImageSize
import me.him188.ani.utils.serialization.BigNum

class BangumiPagedSource(
    private val client: BangumiClient,
    private val query: SubjectSearchQuery,
    private val pageSize: Int = 25,
) : AbstractPageBasedPagedSource<Subject>() {

    override suspend fun nextPageImpl(page: Int): List<Subject> {
        val paged: Paged<Subject>
        if (query.useOldSearchApi) {
            val tmpPaged = client.subjects.searchSubjectsByKeywordsWithOldApi(
                query.keyword,
                convertType(),
                null,
                page * pageSize,
                pageSize,
            )
            paged = Paged(
                total = tmpPaged.total,
                hasMore = tmpPaged.hasMore,
                page = tmpPaged
                    .page.map { convert2Subject(it) },
            )
        } else {
            val resp = client.api.searchSubjects(
                limit = pageSize,
                offset = page * pageSize,
                BangumiSearchSubjectsRequest(
                    keyword = query.keyword,
                    sort = Sort.MATCH,
                    filter = BangumiSearchSubjectsRequestFilter(
                        type = listOf(convertType()),
                        tag = query.tags,
                        airDate = query.airDate.toList().filterNotNull(),
                        rating = query.rating.toList().filterNotNull(),
                        rank = query.rank.toList().filterNotNull(),
                        nsfw = query.nsfw ?: false,
                    ),
                ),
            ).body()
            paged = Paged(
                total = resp.total,
                hasMore = !resp.data.isNullOrEmpty(),
                page = resp.data.orEmpty().map {
                    it.toSubject()
                },
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
            score = BigNum.ZERO,
            rank = legaSub.rank.let { 0 },
            tags = listOf(),
            sourceUrl = legaSub.url.let { "" },
            images = SubjectImages(
                landscapeCommon = BangumiClientImpl.getSubjectImageUrl(
                    legaSub.id,
                    BangumiSubjectImageSize.MEDIUM,
                ),
                largePoster = BangumiClientImpl.getSubjectImageUrl(
                    legaSub.id,
                    BangumiSubjectImageSize.LARGE,
                ),
            ),
            summary = legaSub.summary,
        )
    }

    private fun convertType() = when (query.type) {
        SubjectType.ANIME -> BangumiSubjectType.Anime
    }

}

private fun BangumiSearchSubjects200ResponseDataInner.toSubject(): Subject {
    return Subject(
        id = id,
        originalName = name,
        chineseName = nameCn,
        score = score,
        rank = rank,
        tags = tags.map { it.name to it.count },
        sourceUrl = "",
        images = SubjectImages(
            landscapeCommon = image,
            largePoster = image,
        ),
        summary = summary,
    )
}

private fun BangumiSubject.toSubject(): Subject {
    return Subject(
        id = id,
        originalName = name,
        chineseName = nameCn,
        score = rating.score,
        rank = rating.rank,
        tags = tags.map { it.name to it.count },
        sourceUrl = "",
        images = SubjectImages(
            landscapeCommon = images.common,
            largePoster = images.common,
        ),
        summary = summary,
    )
}
