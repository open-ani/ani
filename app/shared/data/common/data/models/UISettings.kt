package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.ui.collection.EpisodeProgressTheme

@Serializable
@Immutable
data class UISettings(
    val myCollections: MyCollectionsSettings = MyCollectionsSettings.Default,
    val episodeProgress: EpisodeProgressSettings = EpisodeProgressSettings.Default,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        @Stable
        val Default = UISettings()
    }
}

@Serializable
@Immutable
data class MyCollectionsSettings(
    val enableListAnimation: Boolean = true,
) {
    companion object {
        @Stable
        val Default = MyCollectionsSettings()
    }
}

@Serializable
@Immutable
data class EpisodeProgressSettings(
    val theme: EpisodeProgressTheme = EpisodeProgressTheme.Default,
) {
    companion object {
        @Stable
        val Default = EpisodeProgressSettings()
    }
}