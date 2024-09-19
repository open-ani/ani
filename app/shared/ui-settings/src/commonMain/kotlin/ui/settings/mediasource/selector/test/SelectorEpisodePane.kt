/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.source.media.resolver.WebViewVideoExtractor
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceEngine
import me.him188.ani.app.data.source.media.source.web.SelectorSearchConfig
import me.him188.ani.app.platform.Context
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.settings.mediasource.BackgroundSearcher
import me.him188.ani.app.ui.settings.mediasource.launchCollectedInBackground
import me.him188.ani.app.ui.settings.mediasource.selector.edit.MatchVideoSection
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationDefaults
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationState
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.matcher.WebVideoMatcher
import kotlin.coroutines.CoroutineContext

/**
 * 测试 [WebVideoMatcher]
 */
@Stable
class SelectorEpisodeState(
    private val itemState: State<SelectorTestEpisodePresentation?>,
    /**
     * null means loading. Should finally have one.
     */
    matchVideoConfigState: State<SelectorSearchConfig.MatchVideoConfig?>,
    /**
     * null means loading. Should finally have one.
     */
    private val webViewVideoExtractor: State<WebViewVideoExtractor?>,
    private val engine: SelectorMediaSourceEngine,
    backgroundScope: CoroutineScope,
    context: Context,
    flowDispatcher: CoroutineContext = Dispatchers.Default,
) {
    val episodeName: String by derivedStateOf { itemState.value?.name ?: "" }
    val episodeUrl: String by derivedStateOf { itemState.value?.playUrl ?: "" }

    /**
     * 该页面的所有链接
     */
    val searcher =
        BackgroundSearcher(
            backgroundScope,
            testDataState = derivedStateOf { itemState.value?.playUrl to webViewVideoExtractor.value },
        ) { (episodeUrl, extractor) ->
            launchCollectedInBackground {
                if (episodeUrl != null && extractor != null) {
                    extractor.getVideoResourceUrl(context, episodeUrl) {
                        collect(it)
                    }
                }
            }
        }

    @Immutable
    data class MatchResult(
        val originalUrl: String,
        val video: WebVideo?,
    ) {
        @Stable
        fun isMatch() = video != null
    }

    /**
     * 不断更新的匹配结果
     */
    val matchResults: Flow<List<MatchResult>> by derivedStateOf {
        val matchVideoConfig = matchVideoConfigState.value ?: return@derivedStateOf emptyFlow()
        val searchResult = searcher.searchResult ?: return@derivedStateOf emptyFlow()
        searchResult.map { list ->
            list.asSequence()
                .map { original ->
                    MatchResult(original, engine.matchWebVideo(original, matchVideoConfig))
                }
                .distinctBy { it.originalUrl } // O(n) extra space, O(1) time
                .toMutableList() // single list instance construction
                .apply {
                    // sort in-place for better performance
                    sortByDescending { it.isMatch() } // 优先展示匹配的
                }
        }.flowOn(flowDispatcher) // possibly significant computation
    }
}

@Composable
fun SelectorVideoMatcherPaneContent(
    state: SelectorEpisodeState,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
    cardColors: CardColors = AniThemeDefaults.backgroundCardColors(),
    itemColors: ListItemColors = ListItemDefaults.colors(),
) {
    Column(modifier) {
        Card(
            colors = cardColors,
            shape = MaterialTheme.shapes.large,
        ) {
            Row(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) {
                ProvideTextStyle(
                    MaterialTheme.typography.titleLarge,
                ) {
                    Text("匹配视频")
                }
            }

            ListItem(
                headlineContent = { Text(state.episodeName) },
                supportingContent = { Text(state.episodeUrl) },
                colors = ListItemDefaults.colors(containerColor = cardColors.containerColor),
            )
        }

        val list by state.matchResults.collectAsStateWithLifecycle(emptyList())

        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            for (matchResult in list) {
                item(key = matchResult.originalUrl) {
                    val isMatch = matchResult.isMatch()
                    ListItem(
                        headlineContent = { Text(matchResult.originalUrl) },
                        Modifier.animateItem(),
                        supportingContent = {
                            Text(matchResult.video?.m3u8Url ?: "未匹配")
                        },
                        colors = itemColors,
                        trailingContent = {
                            if (isMatch) {
                                Icon(Icons.Rounded.Check, "匹配", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Rounded.Close, "未匹配")
                            }
                        },
                    )
                }
            }
        }
    }
}

enum class SelectorEpisodePaneLayout {
    WithBottomSheet,
    ListOnly, ;

    companion object {
        fun calculate(
            scaffoldValue: ThreePaneScaffoldValue,
        ): SelectorEpisodePaneLayout {
            return when {
                scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded -> {
                    // list 和 extra 同时展开, 也就是大屏环境. list 内包含了配置, 所以我们无需使用 bottom sheet 显示配置
                    ListOnly
                }

                else -> WithBottomSheet
            }
        }
    }
}

/**
 * 测试 [WebVideoMatcher]
 * @param configurationContent [SelectorEpisodePaneDefaults.ConfigurationContent]
 */
@Composable
fun SelectorEpisodePane(
    state: SelectorEpisodeState,
    layout: SelectorEpisodePaneLayout,
    configurationContent: @Composable ColumnScope.(contentPadding: PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    if (layout == SelectorEpisodePaneLayout.ListOnly) {
        SelectorVideoMatcherPaneContent(
            state,
            modifier,
        )
    } else {
        BottomSheetScaffold(
            sheetContent = {
                configurationContent(PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp))
            },
            modifier.padding(contentPadding),
            sheetPeekHeight = 78.dp,
        ) { paddingValues ->
            SelectorVideoMatcherPaneContent(
                state,
                Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            )
        }
    }
}

object SelectorEpisodePaneDefaults

@Suppress("UnusedReceiverParameter")
@Composable
fun SelectorEpisodePaneDefaults.ConfigurationContent(
    state: SelectorConfigurationState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    textFieldShape: Shape = SelectorConfigurationDefaults.textFieldShape,
    verticalSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
) {
    Column(modifier.padding(contentPadding)) {
        Row(Modifier.padding(bottom = 16.dp)) {
            ProvideTextStyle(
                MaterialTheme.typography.titleLarge,
            ) {
                Text("编辑配置")
            }
        }
        SelectorConfigurationDefaults.MatchVideoSection(
            state,
            textFieldShape = textFieldShape,
            verticalSpacing = verticalSpacing,
        )
    }
}
