package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IkarosVideoSubtitle(
    @SerialName("master_attachment_id") val masterAttachmentId:Long,
    @SerialName("attachment_id") val attachmentId:Long,
    val name:String,
    val url:String,
)
