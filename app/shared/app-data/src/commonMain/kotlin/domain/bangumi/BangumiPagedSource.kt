/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.bangumi

import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.app.domain.search.SubjectType
import me.him188.ani.datasources.api.paging.AbstractPageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.BangumiClientImpl
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
) : AbstractPageBasedPagedSource<SubjectInfo>() {

    override suspend fun nextPageImpl(page: Int): List<SubjectInfo> {
        val paged: Paged<SubjectInfo>
        if (query.useOldSearchApi) {
            val tmpPaged = client.getSubjects().searchSubjectsByKeywordsWithOldApi(
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
            val resp = client.getApi().searchSubjects(
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

    private fun convert2Subject(legaSub: BangumiLegacySubject): SubjectInfo {
        return SubjectInfo(
            id = legaSub.id,
            name = legaSub.originalName,
            nameCn = legaSub.chineseName,
            ratingInfo = RatingInfo.Empty.copy(rank = legaSub.rank ?: 0, score = BigNum.ZERO.toString()),
            imageCommon = BangumiClientImpl.getSubjectImageUrl(
                legaSub.id,
                BangumiSubjectImageSize.MEDIUM,
            ),
            imageLarge = BangumiClientImpl.getSubjectImageUrl(
                legaSub.id,
                BangumiSubjectImageSize.LARGE,
            ),
            summary = legaSub.summary,
        )
    }

    private fun convertType() = when (query.type) {
        SubjectType.ANIME -> BangumiSubjectType.Anime
    }

}

private fun BangumiSearchSubjects200ResponseDataInner.toSubject(): SubjectInfo {
    return SubjectInfo(
        id = id,
        name = name,
        nameCn = nameCn,
        ratingInfo = RatingInfo.Empty.copy(rank = rank, score = score.toString()),
        tags = tags.map { Tag(it.name, it.count) },
        imageCommon = image,
        imageLarge = image,
        summary = summary,
    )
}

private fun BangumiSubject.toSubjectInfo(): SubjectInfo {
    return SubjectInfo(
        id = id,
        name = name,
        nameCn = nameCn,
        ratingInfo = RatingInfo.Empty.copy(rank = rating.score.toInt(), score = rating.score.toString()),
        tags = tags.map { Tag(it.name, it.count) },
        imageCommon = images.common,
        imageLarge = images.large,
        summary = summary,
    )
}
