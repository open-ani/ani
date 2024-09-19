/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceArguments
import me.him188.ani.app.data.source.media.source.web.SelectorSearchConfig
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormat
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatFlattened
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.data.source.media.source.web.format.SelectorFormatId
import me.him188.ani.app.data.source.media.source.web.format.SelectorSubjectFormatA
import me.him188.ani.app.ui.foundation.animation.StandardEasing
import me.him188.ani.app.ui.foundation.effects.moveFocusOnEnter
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.theme.EasingDurations
import me.him188.ani.app.ui.settings.danmaku.isValidRegex
import me.him188.ani.app.ui.settings.mediasource.rss.SaveableStorage
import me.him188.ani.app.ui.settings.mediasource.rss.edit.MediaSourceHeadline
import me.him188.ani.utils.xml.QueryParser
import me.him188.ani.utils.xml.parseSelectorOrNull

/**
 * 编辑配置
 */
@Stable
class SelectorConfigurationState(
    private val argumentsStorage: SaveableStorage<SelectorMediaSourceArguments>,
) {
    private val arguments by argumentsStorage.containerState
    val isLoading by derivedStateOf { arguments == null }
    val isSaving by argumentsStorage.isSavingState

    var displayName by argumentsStorage.prop(
        { it.name }, { copy(name = it) },
        "",
    )

    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl by argumentsStorage.prop(
        { it.iconUrl }, { copy(iconUrl = it) },
        "",
    )

    var searchUrl by argumentsStorage.prop(
        { it.searchConfig.searchUrl }, { copy(searchConfig = searchConfig.copy(searchUrl = it)) },
        "",
    )
    val searchUrlIsError by derivedStateOf { searchUrl.isBlank() }

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

@Composable
internal fun SelectorConfigurationPane(
    state: SelectorConfigurationState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
    textFieldShape: Shape = SelectorConfigurationDefaults.textFieldShape,
) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        // 大图标和标题
        MediaSourceHeadline(state.iconUrl, state.displayName)

        Column(
            Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
        ) {
            val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

            Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
                OutlinedTextField(
                    state.displayName, { state.displayName = it },
                    Modifier
                        .fillMaxWidth()
                        .moveFocusOnEnter(),
                    label = { Text("名称*") },
                    placeholder = { Text("设置显示在列表中的名称") },
                    isError = state.displayNameIsError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
                OutlinedTextField(
                    state.iconUrl, { state.iconUrl = it },
                    Modifier
                        .fillMaxWidth()
                        .moveFocusOnEnter(),
                    label = { Text("图标链接") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text(SelectorConfigurationDefaults.STEP_NAME_1)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
                OutlinedTextField(
                    state.searchUrl, { state.searchUrl = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("搜索链接*") },
                    placeholder = {
                        Text(
                            "示例：https://www.nyacg.net/search.html?wd={keyword}",
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    supportingText = {
                        Text(
                            """
                                    替换规则：
                                    {keyword} 替换为条目 (番剧) 名称
                                """.trimIndent(),
                        )
                    },
                    isError = state.searchUrlIsError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
                val conf = state.subjectFormatA
                OutlinedTextField(
                    conf.selectLists, { conf.selectLists = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("提取条目列表") },
                    supportingText = { Text("CSS Selector 表达式。期望返回一些 <a>，每个对应一个条目，将会读取其 href 属性和 text") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    isError = conf.selectListsIsError,
                )
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text(SelectorConfigurationDefaults.STEP_NAME_2)
                }
            }

            SubjectChannelSelectionButtonRow(
                state,
                Modifier.fillMaxWidth(),
            )

            AnimatedContent(
                state.channelFormatId,
                Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
                    .animateContentSize(tween(EasingDurations.standard, easing = StandardEasing)),
                transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
            ) { formatId ->
                SelectorChannelConfigurationColumn(formatId, state, Modifier.fillMaxWidth())
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("过滤设置")
                }
            }

            Column(Modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
                ListItem(
                    headlineContent = { Text("使用条目名称过滤") },
                    Modifier.focusable(false).clickable { state.filterBySubjectName = !state.filterBySubjectName },
                    supportingContent = { Text("要求资源标题包含条目名称。适用于数据源可能搜到无关内容的情况。通常建议开启") },
                    trailingContent = { Switch(state.filterBySubjectName, { state.filterBySubjectName = it }) },
                    colors = listItemColors,
                )
                ListItem(
                    headlineContent = { Text("使用剧集序号过滤") },
                    Modifier.focusable(false).clickable { state.filterByEpisodeSort = !state.filterByEpisodeSort },
                    supportingContent = { Text("要求资源标题包含剧集序号。适用于数据源可能搜到无关内容的情况。通常建议开启") },
                    trailingContent = { Switch(state.filterByEpisodeSort, { state.filterByEpisodeSort = it }) },
                    colors = listItemColors,
                )
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text(SelectorConfigurationDefaults.STEP_NAME_3)
                }
            }

            SelectorConfigurationDefaults.MatchVideoSection(
                state,
                textFieldShape = textFieldShape,
                verticalSpacing = verticalSpacing,
            )

            Row(Modifier.align(Alignment.End).padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.labelMedium,
                    MaterialTheme.colorScheme.outline,
                ) {
                    Text("提示：修改自动保存")
                }
            }
        }

    }
}

@Composable
private fun SubjectChannelSelectionButtonRow(
    state: SelectorConfigurationState,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        @Composable
        fun Btn(
            id: SelectorFormatId, index: Int,
            label: @Composable () -> Unit,
        ) {
            SegmentedButton(
                state.channelFormatId == id,
                { state.channelFormatId = id },
                SegmentedButtonDefaults.itemShape(index, state.allChannelFormats.size),
                icon = { SegmentedButtonDefaults.Icon(state.channelFormatId == id) },
                label = label,
            )
        }

        for ((index, selectorChannelFormat) in state.allChannelFormats.withIndex()) {
            Btn(selectorChannelFormat.id, index) {
                Text(
                    when (selectorChannelFormat) { // type-safe to handle all formats
                        SelectorChannelFormatNoChannel -> "不区分线路"
                        SelectorChannelFormatFlattened -> "多线路扁平"
                    },
                    softWrap = false,
                )
            }
        }
    }
}

object SelectorConfigurationDefaults {
    const val STEP_NAME_1 = "步骤 1：搜索条目"
    const val STEP_NAME_2 = "步骤 2：搜索剧集"
    const val STEP_NAME_3 = "步骤 3：匹配视频"

    val verticalSpacing: Dp
        @Composable
        get() = currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding

    val textFieldShape
        @Composable
        get() = MaterialTheme.shapes.medium
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun SelectorConfigurationDefaults.MatchVideoSection(
    state: SelectorConfigurationState,
    modifier: Modifier = Modifier,
    textFieldShape: Shape = SelectorConfigurationDefaults.textFieldShape,
    verticalSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
        val matchVideoConfig = state.matchVideoConfig
        OutlinedTextField(
            matchVideoConfig.matchVideoUrl, { matchVideoConfig.matchVideoUrl = it },
            Modifier.fillMaxWidth().moveFocusOnEnter(),
            label = { Text("匹配视频链接") },
            supportingText = { Text("从播放页面中加载的所有资源链接中匹配出视频链接的正则表达式。将会使用匹配结果的分组 v") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
            isError = matchVideoConfig.matchVideoUrlIsError,
        )

        val conf = matchVideoConfig.videoHeaders
        OutlinedTextField(
            conf.referer, { conf.referer = it },
            Modifier.fillMaxWidth().moveFocusOnEnter(),
            label = { Text("Referer") },
            supportingText = { Text("HTTP 请求的 Referer") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
        )
        OutlinedTextField(
            conf.userAgent, { conf.userAgent = it },
            Modifier.fillMaxWidth().moveFocusOnEnter(),
            label = { Text("User-Agent") },
            supportingText = { Text("HTTP 请求的 User-Agent") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = textFieldShape,
        )
    }
}
