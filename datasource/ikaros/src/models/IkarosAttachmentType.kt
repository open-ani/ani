package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.Serializable

@Serializable
enum class IkarosAttachmentType {
    File,
    Directory;
}
