package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.Serializable


@Serializable
data class IkarosEpisodeResource(
    val attachmentId:Long,
    val parentAttachmentId:Long,
    val episodeId:Long,
    val url:String,
    val canRead:Boolean,
    val name:String,
    val tags:Set<String>,
)
