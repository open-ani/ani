package me.him188.ani.app.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.subject.CharacterType
import me.him188.ani.app.data.subject.Images
import me.him188.ani.app.data.subject.PersonCareer
import me.him188.ani.app.data.subject.PersonInfo
import me.him188.ani.app.data.subject.RelatedCharacterInfo
import me.him188.ani.datasources.bangumi.BangumiClient
import org.openapitools.client.models.PersonType
import org.openapitools.client.models.RelatedCharacter
import org.openapitools.client.models.CharacterType as BangumiCharacterType
import org.openapitools.client.models.Person as BangumiPerson
import org.openapitools.client.models.PersonCareer as BangumiPersonCareer
import org.openapitools.client.models.PersonImages as BangumiPersonImages

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
        BangumiCharacterType.Character -> CharacterType.Character
        BangumiCharacterType.Mechanic -> CharacterType.Mechanic
        BangumiCharacterType.Ship -> CharacterType.Ship
        BangumiCharacterType.Organization -> CharacterType.Organization
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
        shortSummary = shortSummary,
        locked = locked,
        images = images?.toImages(),
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

private fun PersonType.toPersonType(): PersonType {
    return when (this) {
        PersonType.Individual -> PersonType.Individual
        PersonType.Corporation -> PersonType.Corporation
        PersonType.Association -> PersonType.Association
    }
}
