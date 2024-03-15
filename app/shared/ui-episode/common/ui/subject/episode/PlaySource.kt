package me.him188.ani.app.ui.subject.episode

import androidx.compose.runtime.Immutable
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.Resolution

@Immutable
data class PlaySource(
    val id: String, // must be unique
    val alliance: String,
    val subtitleLanguage: String, // null means raw
    val resolution: Resolution,
    val dataSource: String, // dmhy
    val originalUrl: String,
    val magnetLink: String,
    val originalTitle: String,
    val size: FileSize,
)