package me.him188.ani.app.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.map
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.data.models.subject.RatingCounts
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectCollectionStats
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.data.models.subject.toInfoboxItem
import me.him188.ani.app.data.source.session.OpaqueSession
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.data.source.session.username
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiCount
import me.him188.ani.datasources.bangumi.models.BangumiRating
import me.him188.ani.datasources.bangumi.models.BangumiSubject
import me.him188.ani.datasources.bangumi.models.BangumiSubjectCollectionType
import me.him188.ani.datasources.bangumi.models.BangumiSubjectType
import me.him188.ani.datasources.bangumi.models.BangumiUserEpisodeCollection
import me.him188.ani.datasources.bangumi.models.BangumiUserSubjectCollection
import me.him188.ani.datasources.bangumi.models.BangumiUserSubjectCollectionModifyPayload
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Performs network requests.
 * Use [SubjectManager] instead.
 */
interface BangumiSubjectRepository : Repository {
    suspend fun getSubject(id: Int): ApiResponse<BangumiSubject>
    suspend fun getSubjectBatch(ids: List<Int>): ApiResponse<List<SubjectInfo>>

    fun getSubjectCollections(
        username: String,
        subjectType: BangumiSubjectType? = null,
        subjectCollectionType: BangumiSubjectCollectionType? = null,
    ): PagedSource<BangumiUserSubjectCollection>

    /**
     * 获取用户对这个条目的收藏状态. flow 一定会 emit 至少一个值或抛出异常. 当用户没有收藏这个条目时 emit `null`.
     */
    fun subjectCollectionById(subjectId: Int): Flow<BangumiUserSubjectCollection?>

    fun subjectCollectionTypeById(subjectId: Int): Flow<UnifiedCollectionType>

    suspend fun patchSubjectCollection(subjectId: Int, payload: BangumiUserSubjectCollectionModifyPayload)
    suspend fun deleteSubjectCollection(subjectId: Int)
}

suspend inline fun BangumiSubjectRepository.setSubjectCollectionTypeOrDelete(
    subjectId: Int,
    type: BangumiSubjectCollectionType?
) {
    return if (type == null) {
        deleteSubjectCollection(subjectId)
    } else {
        patchSubjectCollection(subjectId, BangumiUserSubjectCollectionModifyPayload(type))
    }
}

class RemoteBangumiSubjectRepository : BangumiSubjectRepository, KoinComponent {
    private val client: BangumiClient by inject()
    private val sessionManager: SessionManager by inject()
    private val logger = logger(this::class)

    override suspend fun getSubject(id: Int): ApiResponse<BangumiSubject> = runApiRequest {
        client.getApi().getSubjectById(id).body()
    }


//    // graphql
//    @Serializable
//    private data class SubjectFragment(
//        val id: Int,
//        val type: Int,
//        val name: String,
//        val nameCn: String,
//        val images: BangumiImages,
//        val platform: BangumiPlatform,
//        val infobox: List<BangumiInfo>,
//        val summary: String,
//        val volumes: Int,
//        val eps: Int,
//        val collection: SubjectCollection,
//        val series: Boolean,
//        val seriesEntry: Int,
//        val airtime: SubjectAirtime,
//        val rating: SubjectRating,
//        val nsfw: Boolean,
//        val locked: Boolean,
//        val redirect: Int,
//        val tags: List<SubjectTag>,
//        val episodes: List<Episode>,
//        val relations: List<SubjectRelation>
//    )


    override suspend fun getSubjectBatch(ids: List<Int>): ApiResponse<List<SubjectInfo>> {
        if (ids.isEmpty()) {
            return ApiResponse.success(emptyList())
        }

        runApiRequest {
            client.executeGraphQL(
                buildString {
                    appendLine(
                        """
                    fragment SubjectFragment on Subject {
                      id
                      type
                      name
                      name_cn
                      images{large, common}
                      platform{id, type, type_cn}
                      infobox {
                        values {
                          k
                          v
                        }
                        key
                      }
                      summary
                      volumes
                      eps
                      series
                      series_entry
                      collection{__typename}
                      airtime{date}
                      rating{count, rank, score, total}
                      nsfw
                      locked
                      tags{count, name}
                      episodes{airdate, id}
                    }
                    """.trimIndent(),
                    )
                    appendLine("query MyQuery {")
                    for (id in ids) {
                        /*
                            query MyQuery {
                              c10: subject(id: 11200) {
                                ...SubjectFragment
                              }
                            }
                         */
                        append("c")
                        append(id)
                        append(": subject(id: ")
                        append(id)
                        appendLine(") {")
                        appendLine("""  ...SubjectFragment""")
                        appendLine("}")
                    }
                    appendLine("}")
                }.trimIndent(),
            )
        }.map {
        }
        TODO()
    }

    override suspend fun patchSubjectCollection(subjectId: Int, payload: BangumiUserSubjectCollectionModifyPayload) {
        client.getApi().postUserCollection(subjectId, payload)
    }

    override suspend fun deleteSubjectCollection(subjectId: Int) {
        // TODO:  deleteSubjectCollection
    }

    override fun getSubjectCollections(
        username: String,
        subjectType: BangumiSubjectType?,
        subjectCollectionType: BangumiSubjectCollectionType?,
    ): PagedSource<BangumiUserSubjectCollection> {
        return PageBasedPagedSource { page ->
            try {
                val pageSize = 10
                withContext(Dispatchers.IO) {
                    client.getApi().getUserCollectionsByUsername(
                        username,
                        offset = page * pageSize, limit = pageSize,
                        subjectType = subjectType,
                        type = subjectCollectionType,
                    ).body().run {
                        total?.let { setTotalSize(it) }
                        Paged.processPagedResponse(total, pageSize, data)
                    }
                }
            } catch (e: ResponseException) {
                logger.warn("Exception in getCollections, page=$page", e)
                null
            }
        }
    }

    override fun subjectCollectionById(subjectId: Int): Flow<BangumiUserSubjectCollection?> {
        return flow {
            emit(
                try {
                    @OptIn(OpaqueSession::class)
                    client.getApi().getUserCollection(sessionManager.username.first() ?: "-", subjectId).body()
                } catch (e: ResponseException) {
                    if (e.response.status == HttpStatusCode.NotFound) {
                        null
                    } else {
                        throw e
                    }
                },
            )
        }
    }

    override fun subjectCollectionTypeById(subjectId: Int): Flow<UnifiedCollectionType> {
        return flow {
            emit(
                try {
                    @OptIn(OpaqueSession::class)
                    val username = sessionManager.username.first() ?: "-"
                    client.getApi().getUserCollection(username, subjectId).body().type.toCollectionType()
                } catch (e: ResponseException) {
                    if (e.response.status == HttpStatusCode.NotFound) {
                        UnifiedCollectionType.NOT_COLLECTED
                    } else {
                        throw e
                    }
                },
            )
        }
    }
}

fun BangumiUserSubjectCollection.toSubjectCollectionItem(
    subject: BangumiSubject,
    episodes: List<EpisodeCollection>,
): SubjectCollection = SubjectCollection(
    info = subject.toSubjectInfo(),
    episodes = episodes,
    collectionType = type.toCollectionType(),
    selfRatingInfo = toSelfRatingInfo(),
)

fun BangumiUserSubjectCollection.toSelfRatingInfo(): SelfRatingInfo {
    return SelfRatingInfo(
        score = rate,
        comment = comment.takeUnless { it.isNullOrBlank() },
        tags = tags,
        isPrivate = private,
    )
}

fun BangumiSubject.toSubjectInfo(): SubjectInfo {
    return SubjectInfo(
        id = id,
        name = name,
        nameCn = nameCn,
        summary = this.summary,
        nsfw = this.nsfw,
        locked = this.locked,
        platform = this.platform,
        volumes = this.volumes,
        eps = this.eps,
        totalEpisodes = this.totalEpisodes,
        airDateString = this.date,
        tags = this.tags.map { Tag(it.name, it.count) }.sortedByDescending { it.count },
        infobox = this.infobox?.map { it.toInfoboxItem() }.orEmpty(),
        imageCommon = this.images.common,
        imageLarge = this.images.large,
        collection = this.collection.run {
            SubjectCollectionStats(
                wish = wish,
                doing = doing,
                done = collect,
                onHold = onHold,
                dropped = dropped,
            )
        },
        ratingInfo = this.rating.toRatingInfo(),
    )
}


private fun BangumiRating.toRatingInfo(): RatingInfo = RatingInfo(
    rank = rank,
    total = total,
    count = count.toRatingCounts(),
    score = score.toString(),
)

private fun BangumiCount.toRatingCounts() = RatingCounts(
    intArrayOf(
        _1 ?: 0,
        _2 ?: 0,
        _3 ?: 0,
        _4 ?: 0,
        _5 ?: 0,
        _6 ?: 0,
        _7 ?: 0,
        _8 ?: 0,
        _9 ?: 0,
        _10 ?: 0,
    ),
)

fun BangumiUserEpisodeCollection.toEpisodeCollection(): EpisodeCollection {
    return EpisodeCollection(
        episodeInfo = episode.toEpisodeInfo(),
        collectionType = type.toCollectionType(),
    )
}
