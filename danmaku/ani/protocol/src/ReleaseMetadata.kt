package me.him188.ani.danmaku.protocol

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseMetadata(
    val files: List<ReleaseFile>,
)

@Serializable
data class ReleaseFile(
    val name: String,
    val hash: String,
)
