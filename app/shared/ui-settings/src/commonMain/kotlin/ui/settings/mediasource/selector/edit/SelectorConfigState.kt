/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceArguments
import me.him188.ani.app.data.source.media.source.web.SelectorSearchConfig
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormat
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatFlattened
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.data.source.media.source.web.format.SelectorSubjectFormatA
import me.him188.ani.app.ui.settings.danmaku.isValidRegex
import me.him188.ani.app.ui.settings.mediasource.rss.SaveableStorage
import me.him188.ani.utils.xml.QueryParser
import me.him188.ani.utils.xml.parseSelectorOrNull

/**
 * 编辑配置
 */
@Stable
class SelectorConfigState(
    private val argumentsStorage: SaveableStorage<SelectorMediaSourceArguments>,
) {
    private val arguments by argumentsStorage.containerState
    val isLoading by derivedStateOf { arguments == null }
    val isSaving by argumentsStorage.isSavingState

    var displayName by argumentsStorage.prop(
        { it.name }, { copy(name = it) },
        SelectorMediaSourceArguments.Default.name,
    )

    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl by argumentsStorage.prop(
        { it.iconUrl }, { copy(iconUrl = it) },
        SelectorMediaSourceArguments.Default.iconUrl,
    )

    var searchUrl by argumentsStorage.prop(
        { it.searchConfig.searchUrl }, { copy(searchConfig = searchConfig.copy(searchUrl = it)) },
        SelectorMediaSourceArguments.Default.searchConfig.searchUrl,
    )
    val searchUrlIsError by derivedStateOf { searchUrl.isBlank() }

    var searchUseOnlyFirstWord by argumentsStorage.prop(
        { it.searchConfig.searchUseOnlyFirstWord },
        { copy(searchConfig = searchConfig.copy(searchUseOnlyFirstWord = it)) },
        SelectorMediaSourceArguments.Default.searchConfig.searchUseOnlyFirstWord,
    )

    // region SubjectFormat

    val subjectFormatA = SubjectFormatAConfig()

    @Stable
    inner class SubjectFormatAConfig {
        private fun <T : Any> prop(
            get: (SelectorSubjectFormatA.Config) -> T,
            set: SelectorSubjectFormatA.Config.(T) -> SelectorSubjectFormatA.Config,
        ) = argumentsStorage.prop(
            { it.searchConfig.selectorSubjectFormatA.let(get) },
            {
                copy(
                    searchConfig = searchConfig.copy(
                        selectorSubjectFormatA = searchConfig.selectorSubjectFormatA.set(it),
                    ),
                )
            },
            SelectorMediaSourceArguments.Default.searchConfig.selectorSubjectFormatA.let(get),
        )

        var selectLists by prop({ it.selectLists }, { copy(selectLists = it) })
        val selectListsIsError by derivedStateOf {
            QueryParser.parseSelectorOrNull(selectLists) == null
        }
        var preferShorterName by prop({ it.preferShorterName }, { copy(preferShorterName = it) })
    }

    // endregion

    // region ChannelFormat

    var channelFormatId by argumentsStorage.prop(
        { it.searchConfig.channelFormatId }, { copy(searchConfig = searchConfig.copy(channelFormatId = it)) },
        SelectorMediaSourceArguments.Default.searchConfig.channelFormatId,
    )
    val allChannelFormats get() = SelectorChannelFormat.entries

    val channelFormatIndexed = ChannelFormatIndexedConfig()

    @Stable
    inner class ChannelFormatIndexedConfig {
        private fun <T : Any> prop(
            get: (SelectorChannelFormatFlattened.Config) -> T,
            set: SelectorChannelFormatFlattened.Config.(T) -> SelectorChannelFormatFlattened.Config,
        ) = argumentsStorage.prop(
            { it.searchConfig.selectorChannelFormatFlattened.let(get) },
            {
                copy(
                    searchConfig = searchConfig.copy(
                        selectorChannelFormatFlattened = searchConfig.selectorChannelFormatFlattened.set(it),
                    ),
                )
            },
            SelectorMediaSourceArguments.Default.searchConfig.selectorChannelFormatFlattened.let(get),
        )

        var selectChannels by prop({ it.selectChannels }, { copy(selectChannels = it) })
        val selectChannelsIsError by derivedStateOf {
            QueryParser.parseSelectorOrNull(selectChannels) == null
        }
        var selectLists by prop({ it.selectLists }, { copy(selectLists = it) })
        val selectListsIsError by derivedStateOf {
            QueryParser.parseSelectorOrNull(selectLists) == null
        }
        var matchEpisodeSortFromName by prop({ it.matchEpisodeSortFromName }, { copy(matchEpisodeSortFromName = it) })
        val matchEpisodeSortFromNameIsError by derivedStateOf {
            matchEpisodeSortFromName.isBlank() || !isValidRegex(matchEpisodeSortFromName)
        }
    }

    val channelFormatNoChannel = ChannelFormatNoChannelConfig()

    @Stable
    inner class ChannelFormatNoChannelConfig {
        private fun <T : Any> prop(
            get: (SelectorChannelFormatNoChannel.Config) -> T,
            set: SelectorChannelFormatNoChannel.Config.(T) -> SelectorChannelFormatNoChannel.Config,
        ) = argumentsStorage.prop(
            { it.searchConfig.selectorChannelFormatNoChannel.let(get) },
            {
                copy(
                    searchConfig = searchConfig.copy(
                        selectorChannelFormatNoChannel = searchConfig.selectorChannelFormatNoChannel.set(it),
                    ),
                )
            },
            SelectorMediaSourceArguments.Default.searchConfig.selectorChannelFormatNoChannel.let(get),
        )

        var selectEpisodes by prop({ it.selectEpisodes }, { copy(selectEpisodes = it) })
        val selectEpisodesIsError by derivedStateOf { QueryParser.parseSelectorOrNull(selectEpisodes) == null }
        var matchEpisodeSortFromName by prop(
            { it.matchEpisodeSortFromName },
            { copy(matchEpisodeSortFromName = it) },
        )
        val matchEpisodeSortFromNameIsError by derivedStateOf {
            matchEpisodeSortFromName.isBlank() || !isValidRegex(matchEpisodeSortFromName)
        }
    }

    // endregion

    var filterByEpisodeSort by argumentsStorage.prop(
        { it.searchConfig.filterByEpisodeSort }, { copy(searchConfig = searchConfig.copy(filterByEpisodeSort = it)) },
        SelectorMediaSourceArguments.Default.searchConfig.filterByEpisodeSort,
    )
    var filterBySubjectName by argumentsStorage.prop(
        { it.searchConfig.filterBySubjectName }, { copy(searchConfig = searchConfig.copy(filterBySubjectName = it)) },
        SelectorMediaSourceArguments.Default.searchConfig.filterBySubjectName,
    )

    val matchVideoConfig: MatchVideoConfig = MatchVideoConfig()

    @Stable
    inner class MatchVideoConfig {
        private fun <T : Any> prop(
            get: (SelectorSearchConfig.MatchVideoConfig) -> T,
            set: SelectorSearchConfig.MatchVideoConfig.(T) -> SelectorSearchConfig.MatchVideoConfig,
        ) = argumentsStorage.prop(
            { it.searchConfig.matchVideo.let(get) },
            {
                copy(
                    searchConfig = searchConfig.copy(
                        matchVideo = searchConfig.matchVideo.set(it),
                    ),
                )
            },
            SelectorMediaSourceArguments.Default.searchConfig.matchVideo.let(get),
        )

        var matchVideoUrl by prop(
            { it.matchVideoUrl }, { copy(matchVideoUrl = it) },
        )
        val matchVideoUrlIsError by derivedStateOf {
            matchVideoUrl.isBlank() || !isValidRegex(matchVideoUrl)
        }

        var enableNestedUrl by prop(
            { it.enableNestedUrl }, { copy(enableNestedUrl = it) },
        )
        var matchNestedUrl by prop(
            { it.matchNestedUrl }, { copy(matchNestedUrl = it) },
        )
        val matchNestedUrlIsError by derivedStateOf {
            matchNestedUrl.isBlank() || !isValidRegex(matchNestedUrl)
        }

        val videoHeaders = HeadersConfig()

        @Stable
        inner class HeadersConfig {
            private fun <T : Any> prop(
                get: (SelectorSearchConfig.VideoHeaders) -> T,
                set: SelectorSearchConfig.VideoHeaders.(T) -> SelectorSearchConfig.VideoHeaders,
            ) = this@MatchVideoConfig.prop(
                { it.addHeadersToVideo.let(get) },
                { copy(addHeadersToVideo = addHeadersToVideo.set(it)) },
            )

            var referer by prop(
                { it.referer }, { copy(referer = referer) },
            )
            var userAgent by prop(
                { it.userAgent }, { copy(userAgent = userAgent) },
            )
        }
    }

    val searchConfigState = derivedStateOf {
        argumentsStorage.container?.searchConfig
    }
}