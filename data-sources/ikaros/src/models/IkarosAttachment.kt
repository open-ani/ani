package models

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.ikaros.models.IkarosAttachmentType

@Serializable
class IkarosAttachment {
    val id: Long = 0
    val parentId: Long = 0
    val type: IkarosAttachmentType = IkarosAttachmentType.File

    /**
     * HTTP path.
     */
    val url: String? = null

    /**
     * Attachment logic path.
     */
    val path: String? = null

    /**
     * File path in file system.
     */
    val fsPath: String? = null

    /**
     * filename with postfix.
     */
    val name: String? = null
    val size: Long = 0
    val updateTime: String? = null
}
