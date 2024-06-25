package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import org.openapitools.client.models.PersonType

@Immutable
class RelatedCharacterInfo(
    val id: Int,
    val name: String,
    val type: CharacterType,
    val relation: String,
    val images: Images?,
    val actors: List<PersonInfo>,
)

@Immutable
enum class CharacterType {
    Character,
    Mechanic,
    Ship,
    Organization;
}

@Immutable
data class Images(
    val large: String,
    val medium: String,
    val small: String,
    val grid: String,
)

@Immutable
class PersonInfo(
    val id: Int,
    val name: String,
    val type: PersonType,
    val careers: List<PersonCareer>,
    val shortSummary: String,
    val locked: Boolean,
    val images: Images?,
)

@Immutable
enum class PersonType {
    Individual,
    Corporation,
    Association;
}

@Immutable
enum class PersonCareer {
    PRODUCER,
    MANGAKA,
    ARTIST,
    SEIYU,
    WRITER,
    ILLUSTRATOR,
    ACTOR;
}
