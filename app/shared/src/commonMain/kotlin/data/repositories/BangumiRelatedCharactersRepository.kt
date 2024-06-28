package me.him188.ani.app.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.subject.CharacterType
import me.him188.ani.app.data.subject.Images
import me.him188.ani.app.data.subject.PersonCareer
import me.him188.ani.app.data.subject.PersonInfo
import me.him188.ani.app.data.subject.PersonType
import me.him188.ani.app.data.subject.RelatedCharacterInfo
import me.him188.ani.app.data.subject.RelatedPersonInfo
import me.him188.ani.datasources.bangumi.BangumiClient
import org.openapitools.client.models.RelatedCharacter
import org.openapitools.client.models.RelatedPerson
import org.openapitools.client.models.CharacterType as BangumiCharacterType
import org.openapitools.client.models.Person as BangumiPerson
import org.openapitools.client.models.PersonCareer as BangumiPersonCareer
import org.openapitools.client.models.PersonImages as BangumiPersonImages
import org.openapitools.client.models.PersonType as BangumiPersonType

class BangumiRelatedCharactersRepository(
    private val client: BangumiClient,
) {
    fun relatedCharactersFlow(subjectId: Int): Flow<List<RelatedCharacterInfo>> {
        return flow {
            emit(
                withContext(Dispatchers.IO) {
                    client.api.getRelatedCharactersBySubjectId(subjectId)
                }.map {
                    it.toRelatedCharacterInfo()
                },
            )
        }
    }

    fun relatedPersonsFlow(subjectId: Int): Flow<List<RelatedPersonInfo>> {
        return flow {
            emit(
                withContext(Dispatchers.IO) {
                    client.api.getRelatedPersonsBySubjectId(subjectId)
                }.map {
                    it.toRelatedPersonInfo()
                }.asReversed(),
            )
        }
    }
}

private fun RelatedPerson.toRelatedPersonInfo(): RelatedPersonInfo {
    return RelatedPersonInfo(
        personInfo = PersonInfo(
            id = id,
            name = name,
            type = type.toPersonType(),
            careers = career.map { it.toPersonCareer() },
            images = images?.toImages(),
            locked = null,
            shortSummary = null,
        ),
        relation = relation,
    )
}

private fun RelatedCharacter.toRelatedCharacterInfo(): RelatedCharacterInfo {
    return RelatedCharacterInfo(
        id = id,
        name = name,
        type = type.toCharacterType(),
        relation = relation,
        images = images?.toImages(),
        actors = actors?.map { it.toPersonInfo() }.orEmpty(),
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

private fun BangumiPerson.toPersonInfo(): PersonInfo {
    return PersonInfo(
        id = id,
        name = name,
        type = type.toPersonType(),
        careers = career.map { it.toPersonCareer() },
        images = images?.toImages(),
        shortSummary = shortSummary,
        locked = locked,
    )
}

private fun BangumiPersonCareer.toPersonCareer(): PersonCareer {
    return when (this) {
        BangumiPersonCareer.producer -> PersonCareer.PRODUCER
        BangumiPersonCareer.mangaka -> PersonCareer.MANGAKA
        BangumiPersonCareer.artist -> PersonCareer.ARTIST
        BangumiPersonCareer.seiyu -> PersonCareer.SEIYU
        BangumiPersonCareer.writer -> PersonCareer.WRITER
        BangumiPersonCareer.illustrator -> PersonCareer.ILLUSTRATOR
        BangumiPersonCareer.actor -> PersonCareer.ACTOR
    }
}

private fun BangumiPersonType.toPersonType(): PersonType {
    return when (this) {
        BangumiPersonType.Individual -> PersonType.Individual
        BangumiPersonType.Corporation -> PersonType.Corporation
        BangumiPersonType.Association -> PersonType.Association
    }
}
