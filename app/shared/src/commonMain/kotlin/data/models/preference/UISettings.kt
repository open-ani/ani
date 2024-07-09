package me.him188.ani.app.data.models.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.ui.subject.collection.progress.EpisodeProgressTheme

@Serializable
@Immutable
data class UISettings(
    val theme: ThemeSettings = ThemeSettings.Default,
    val myCollections: MyCollectionsSettings = MyCollectionsSettings.Default,
    val searchSettings: SearchSettings = SearchSettings.Default,
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
data class ThemeSettings(
    val kind: ThemeKind = ThemeKind.AUTO,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        @Stable
        val Default = ThemeSettings()
    }
}

@Serializable
enum class ThemeKind {
    LIGHT, DARK, AUTO
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
data class SearchSettings(
    val enableNewSearchSubjectApi: Boolean = false,
) {
    companion object {
        @Stable
        val Default = SearchSettings()
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