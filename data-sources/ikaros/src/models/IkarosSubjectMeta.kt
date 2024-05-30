package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IkarosSubjectMeta (
    val id: Long,
    val name: String?,
    @SerialName("name_cn") val nameCn: String?,
    val infobox: String?,
    val summary: String?,
    val nsfw: Boolean?,
    @SerialName("airTime") val airTime: String?,
    val cover: String?,
    @SerialName("collection_status") val collectionType: IkarosCollectionType?,
    val canRead:Boolean = true,
)