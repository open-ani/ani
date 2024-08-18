package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Immutable

@Immutable
class RelatedCharacterInfo(
    val id: Int,
    val originalName: String,
    val type: CharacterType,
    val relation: String,
    val images: Images?,
    val actors: List<PersonInfo>,
    val chineseName: String = "",
) {
    val displayName get() = chineseName.takeIf { it.isNotBlank() } ?: originalName

    fun isMainCharacter() = relation == "主角"

    companion object {
        fun sortList(characterList: List<RelatedCharacterInfo>): List<RelatedCharacterInfo> {
            return characterList.sortedByDescending {
                when (it.relation) {
                    "主角" -> 10
                    "配角" -> 9
                    "客串" -> 8
                    else -> 0
                }
            }
        }
    }
}

@Immutable
enum class CharacterType {
    CHARACTER,
    MECHANIC,
    SHIP,
    ORGANIZATION;
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
    val originalName: String,
    val type: PersonType,
    val careers: List<PersonCareer>,
    val images: Images?,
    val shortSummary: String?,
    val locked: Boolean?,
    val chineseName: String = "",
) {
    val displayName get() = chineseName.takeIf { it.isNotBlank() } ?: originalName
}

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
