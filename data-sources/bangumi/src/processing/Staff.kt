package me.him188.animationgarden.datasources.bangumi.processing

import org.openapitools.client.models.RelatedPerson


class Staff(
    val company: String,
    val selectedRelatedPersons: List<RelatedPerson>,
)

// "音乐制作"
val selectedRelations: List<Regex> = listOf(
    Regex("动画制作"),
    Regex("导演|监督"),
    Regex("编剧"),
    Regex("音乐"),
    Regex("人物设定"),
    Regex("系列构成"),
    Regex("动作作画监督|动作导演"),
    Regex("美术设计"),
    Regex("主题歌(.*)?"),
)

fun List<RelatedPerson>.sortByRelation(): List<RelatedPerson> {
    val original = this
    return buildList {
        for (selectedRelation in selectedRelations) {
            for (relatedPerson in original) {
                if (relatedPerson.relation.matches(selectedRelation)) {
                    add(relatedPerson)
                }
            }
        }
    }
}