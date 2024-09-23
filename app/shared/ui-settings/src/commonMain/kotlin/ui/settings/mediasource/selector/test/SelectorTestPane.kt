/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.test

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.interaction.nestedScrollWorkaround
import me.him188.ani.app.ui.foundation.layout.ConnectedScrollState
import me.him188.ani.app.ui.foundation.layout.PaddingValuesSides
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding
import me.him188.ani.app.ui.foundation.layout.connectedScroll
import me.him188.ani.app.ui.foundation.layout.only
import me.him188.ani.app.ui.foundation.layout.rememberConnectedScrollState
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressIndicator
import me.him188.ani.app.ui.settings.mediasource.EditMediaSourceTestDataCardDefaults
import me.him188.ani.app.ui.settings.mediasource.RefreshIndicatedHeadlineRow
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationDefaults

/**
 * 测试数据源. 编辑
 */
@Composable
fun SharedTransitionScope.SelectorTestPane(
    state: SelectorTestState,
    onViewEpisode: (SelectorTestEpisodePresentation) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    connectedScrollState: ConnectedScrollState = rememberConnectedScrollState(),
) {
    val verticalSpacing = currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding
    Column(
        modifier
            .padding(contentPadding.only(PaddingValuesSides.Top))
            .clipToBounds(),
    ) {
        Column(
            Modifier.connectedScroll(connectedScrollState)
                .padding(contentPadding.only(PaddingValuesSides.Horizontal)),
        ) {
            Text(
                "测试数据源",
                style = MaterialTheme.typography.headlineSmall,
            )

            EditTestDataCard(
                state,
                Modifier
                    .padding(top = verticalSpacing)
                    .fillMaxWidth(),
            )

            RefreshIndicatedHeadlineRow(
                headline = { Text(SelectorConfigurationDefaults.STEP_NAME_1) },
                onRefresh = { state.subjectSearcher.restartCurrentSearch() },
                result = state.subjectSearchSelectResult,
                Modifier.padding(top = verticalSpacing),
            )

            Box(Modifier.height(12.dp), contentAlignment = Alignment.Center) {
                FastLinearProgressIndicator(
                    state.subjectSearcher.isSearching,
                    delayMillis = 0,
                    minimumDurationMillis = 300,
                )
            }

            AnimatedContent(
                state.subjectSearchSelectResult,
                transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
            ) { result ->
                if (result is SelectorTestSearchSubjectResult.Success) {
                    SelectorTestSubjectResultLazyRow(
                        items = result.subjects,
                        state.selectedSubjectIndex,
                        onSelect = { index, _ ->
                            state.selectedSubjectIndex = index
                        },
                        modifier = Modifier.padding(top = verticalSpacing - 8.dp),
                    )
                }
            }
        }

        AnimatedContent(
            state.selectedSubject,
            Modifier.padding(contentPadding.only(PaddingValuesSides.Horizontal)),
            transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
        ) { selectedSubjectIndex ->
            if (selectedSubjectIndex != null) {
                Column {
                    RefreshIndicatedHeadlineRow(
                        headline = { Text(SelectorConfigurationDefaults.STEP_NAME_2) },
                        onRefresh = { state.episodeListSearcher.restartCurrentSearch() },
                        result = state.episodeListSearchSelectResult,
                        Modifier.padding(top = verticalSpacing),
                    )

                    Box(Modifier.height(12.dp), contentAlignment = Alignment.Center) {
                        FastLinearProgressIndicator(
                            state.episodeListSearcher.isSearching,
                            delayMillis = 0,
                            minimumDurationMillis = 300,
                        )
                    }
                }
            }
        }

        if (state.selectedSubject != null) {
            AnimatedContent(
                state.episodeListSearchSelectResult,
                transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
            ) { result ->
                if (result is SelectorTestEpisodeListResult.Success) {
                    val staggeredGridState = rememberLazyStaggeredGridState()
                    SelectorTestEpisodeListGrid(
                        result.episodes,
                        modifier = Modifier.padding(top = verticalSpacing - 8.dp)
                            .nestedScroll(connectedScrollState.nestedScrollConnection)
                            .nestedScrollWorkaround(staggeredGridState, connectedScrollState),
                        state = staggeredGridState,
                        contentPadding = contentPadding.only(PaddingValuesSides.Horizontal + PaddingValuesSides.Bottom),
                    ) { episode ->
                        SelectorTestEpisodeListGridDefaults.EpisodeCard(
                            episode,
                            { onViewEpisode(episode) },
                            Modifier.sharedBounds(rememberSharedContentState(episode.id), animatedVisibilityScope),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditTestDataCard(
    state: SelectorTestState,
    modifier: Modifier = Modifier,
) {
    with(EditMediaSourceTestDataCardDefaults) {
        Card(
            modifier,
            shape = cardShape,
            colors = cardColors,
        ) {
            FlowRow {
                KeywordTextField(state, Modifier.weight(1f))
                EpisodeSortTextField(state, Modifier.weight(1f))
            }
        }
    }
}
