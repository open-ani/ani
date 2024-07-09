package me.him188.ani.app.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.him188.ani.app.data.model.subject.CharacterType
import me.him188.ani.app.data.model.subject.Images
import me.him188.ani.app.data.model.subject.PersonCareer
import me.him188.ani.app.data.model.subject.PersonInfo
import me.him188.ani.app.data.model.subject.PersonType
import me.him188.ani.app.data.model.subject.RelatedCharacterInfo
import me.him188.ani.app.data.model.subject.RelatedPersonInfo
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiCharacterType
import me.him188.ani.datasources.bangumi.models.BangumiPerson
import me.him188.ani.datasources.bangumi.models.BangumiPersonCareer
import me.him188.ani.datasources.bangumi.models.BangumiPersonImages
import me.him188.ani.datasources.bangumi.models.BangumiPersonType
import me.him188.ani.datasources.bangumi.models.BangumiRelatedCharacter
import me.him188.ani.datasources.bangumi.models.BangumiRelatedPerson
import me.him188.ani.utils.coroutines.flows.runOrEmitEmptyList

@Serializable
data class QInfobox(
    val key: String,
    val values: List<QInfoboxValue> = emptyList(),
)

@Serializable
data class QInfoboxValue(
    val k: String?,
    val v: String,
)

private val json = Json {
    ignoreUnknownKeys = true
}

class BangumiRelatedCharactersRepository(
    private val client: BangumiClient,
) {
    @Serializable
    data class QCharacterOrPerson(
        val id: Int,
        val infobox: List<QInfobox>
    ) {
        val chineseName: String?
            get() {
                return infobox.find { it.key == "简体中文名" }?.values?.firstOrNull()?.v
            }
    }

    /**
     * 查询该条目的角色列表. 返回的 flow 至少会 emit 一个 list.
     */
    fun relatedCharactersFlow(subjectId: Int): Flow<List<RelatedCharacterInfo>> {
        return flow {
            val characters = runOrEmitEmptyList {
                withContext(Dispatchers.IO) {
                    client.api.getRelatedCharactersBySubjectId(subjectId).body()
                }
            }

            // 查 GraphQL 要一秒, 先按日文显示 
            emit(
                characters.map { character ->
                    character.toRelatedCharacterInfo(
                        chineseName = "",
                        getPersonChineseName = { "" },
                    )
                },
            )
            coroutineScope {
                val qCharacters = async { queryGraphQLCharacters(characters.asSequence().map { it.id }) }
                val qPersons = queryGraphQLPersons(
                    characters.asSequence().flatMap { c -> c.actors.orEmpty().map { it.id } },
                )
                emit(
                    characters.map { character ->
                        character.toRelatedCharacterInfo(
                            chineseName = qCharacters.await().find { it.id == character.id }?.chineseName ?: "",
                            getPersonChineseName = { person ->
                                qPersons.find { it.id == person }?.chineseName ?: ""
                            },
                        )
                    },
                )
            }
        }
    }

    private suspend fun queryGraphQLCharacters(
        ids: Sequence<Int>,
    ) = client.executeGraphQL(
        """
            query MyQuery {
              ${ids.distinct().joinToString("\n") { "c${it}: character(id: ${it}) { ...CharacterFragment }" }}
            }
    
            fragment CharacterFragment on Character {
              infobox {
                values {
                  k
                  v
                }
                key
              }
              id
            }
        """.trimIndent(),
    )["data"]!!.jsonObject.values.map {
        json.decodeFromJsonElement(QCharacterOrPerson.serializer(), it)
    }

    private suspend fun queryGraphQLPersons(
        ids: Sequence<Int>,
    ) = client.executeGraphQL(
        """
            query MyQuery {
              ${ids.joinToString("\n") { "c${it}: person(id: ${it}) { ...CharacterFragment }" }}
            }
    
            fragment CharacterFragment on Person {
              infobox {
                values {
                  k
                  v
                }
                key
              }
              id
            }
        """.trimIndent(),
    )["data"]!!.jsonObject.values.map {
        json.decodeFromJsonElement(QCharacterOrPerson.serializer(), it)
    }


    /**
     * 查询该条目的 staff. 返回的 flow 至少会 emit 一个 list.
     */
    fun relatedPersonsFlow(subjectId: Int): Flow<List<RelatedPersonInfo>> {
        return flow {
            val persons = runOrEmitEmptyList {
                withContext(Dispatchers.IO) {
                    client.api.getRelatedPersonsBySubjectId(subjectId).body()
                }
            }
            emit(
                persons.map { character ->
                    character.toRelatedPersonInfo(
                        chineseName = "",
                    )
                },
            )
            // 查 GraphQL 要一秒, 先按日文显示 
            val q = queryGraphQLPersons(persons.asSequence().map { it.id })
            emit(
                persons.map { character ->
                    character.toRelatedPersonInfo(
                        chineseName = q.find { it.id == character.id }?.chineseName ?: "",
                    )
                },
            )
        }
    }
}

private fun BangumiRelatedPerson.toRelatedPersonInfo(chineseName: String): RelatedPersonInfo {
    return RelatedPersonInfo(
        personInfo = PersonInfo(
            id = id,
            originalName = name,
            type = type.toPersonType(),
            careers = career.map { it.toPersonCareer() },
            images = images?.toImages(),
            locked = null,
            shortSummary = null,
            chineseName = chineseName,
        ),
        relation = relation,
    )
}

private fun BangumiRelatedCharacter.toRelatedCharacterInfo(
    chineseName: String,
    getPersonChineseName: (Int) -> String = { "" },
): RelatedCharacterInfo {
    return RelatedCharacterInfo(
        id = id,
        originalName = name,
        type = type.toCharacterType(),
        relation = relation,
        images = images?.toImages(),
        actors = actors?.map { it.toPersonInfo(getPersonChineseName(it.id)) }.orEmpty(),
        chineseName = chineseName,
    )
}

private fun BangumiCharacterType.toCharacterType(): CharacterType {
    return when (this) {
        BangumiCharacterType.Character -> CharacterType.CHARACTER
        BangumiCharacterType.Mechanic -> CharacterType.MECHANIC
        BangumiCharacterType.Ship -> CharacterType.SHIP
        BangumiCharacterType.Organization -> CharacterType.ORGANIZATION
    }
}

private fun BangumiPersonImages.toImages(): Images {
    return Images(
        large = large,
        medium = medium,
        small = small,
        grid = grid,
    )
}

private fun BangumiPerson.toPersonInfo(chineseName: String): PersonInfo {
    return PersonInfo(
        id = id,
        originalName = name,
        type = type.toPersonType(),
        careers = career.map { it.toPersonCareer() },
        images = images?.toImages(),
        shortSummary = shortSummary,
        locked = locked,
        chineseName = chineseName,
    )
}

private fun BangumiPersonCareer.toPersonCareer(): PersonCareer {
    return when (this) {
        BangumiPersonCareer.PRODUCER -> PersonCareer.PRODUCER
        BangumiPersonCareer.MANGAKA -> PersonCareer.MANGAKA
        BangumiPersonCareer.ARTIST -> PersonCareer.ARTIST
        BangumiPersonCareer.SEIYU -> PersonCareer.SEIYU
        BangumiPersonCareer.WRITER -> PersonCareer.WRITER
        BangumiPersonCareer.ILLUSTRATOR -> PersonCareer.ILLUSTRATOR
        BangumiPersonCareer.ACTOR -> PersonCareer.ACTOR
    }
}

private fun BangumiPersonType.toPersonType(): PersonType {
    return when (this) {
        BangumiPersonType.Individual -> PersonType.Individual
        BangumiPersonType.Corporation -> PersonType.Corporation
        BangumiPersonType.Association -> PersonType.Association
    }
}
