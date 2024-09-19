/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.him188.ani.app.data.source.media.resolver.WebViewVideoExtractor
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceArguments
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceEngine
import me.him188.ani.app.platform.Context
import me.him188.ani.app.ui.foundation.layout.AnimatedPane1
import me.him188.ani.app.ui.foundation.layout.isWidthCompact
import me.him188.ani.app.ui.foundation.layout.materialWindowMarginPadding
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.settings.mediasource.rss.SaveableStorage
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationPane
import me.him188.ani.app.ui.settings.mediasource.selector.edit.SelectorConfigurationState
import me.him188.ani.app.ui.settings.mediasource.selector.episode.ConfigurationContent
import me.him188.ani.app.ui.settings.mediasource.selector.episode.SelectorEpisodePane
import me.him188.ani.app.ui.settings.mediasource.selector.episode.SelectorEpisodePaneDefaults
import me.him188.ani.app.ui.settings.mediasource.selector.episode.SelectorEpisodePaneLayout
import me.him188.ani.app.ui.settings.mediasource.selector.episode.SelectorEpisodeState
import me.him188.ani.app.ui.settings.mediasource.selector.test.SelectorTestPane
import me.him188.ani.app.ui.settings.mediasource.selector.test.SelectorTestState

class EditSelectorMediaSourceState(
    argumentsStorage: SaveableStorage<SelectorMediaSourceArguments>,
    engine: SelectorMediaSourceEngine,
    webViewVideoExtractor: State<WebViewVideoExtractor?>,
    backgroundScope: CoroutineScope,
    context: Context,
    flowDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    internal val configurationState: SelectorConfigurationState = SelectorConfigurationState(argumentsStorage)

    internal val testState: SelectorTestState =
        SelectorTestState(configurationState.searchConfigState, engine, backgroundScope)

    internal val episodeState: SelectorEpisodeState = SelectorEpisodeState(
        itemState = derivedStateOf { testState.viewingItem },
        matchVideoConfigState = derivedStateOf { configurationState.searchConfigState.value?.matchVideo },
        webViewVideoExtractor = webViewVideoExtractor,
        engine = engine,
        backgroundScope = backgroundScope,
        context = context,
        flowDispatcher = flowDispatcher,
    )
}

@Composable
fun EditSelectorMediaSourcePage(
    vm: SelectorMediaSourceConfigurationViewModel,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<Nothing> = rememberListDetailPaneScaffoldNavigator(),
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val state by vm.state.collectAsStateWithLifecycle(null)
    state?.let {
        EditSelectorMediaSourcePage(it, modifier, navigator, windowInsets)
    }
}

@Composable
fun EditSelectorMediaSourcePage(
    state: EditSelectorMediaSourceState,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<Nothing> = rememberListDetailPaneScaffoldNavigator(),
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.configurationState.displayName) },
                navigationIcon = { TopAppBarGoBackButton() },
                actions = {
                    if (currentWindowAdaptiveInfo().isWidthCompact) {
                        TextButton({ navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }) {
                            Text("测试")
                        }
                    }
                },
                windowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
        },
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { paddingValues ->
        BackHandler(navigator.canNavigateBack()) {
            navigator.navigateBack()
        }
        ListDetailPaneScaffold(
            navigator.scaffoldDirective,
            navigator.scaffoldValue,
            listPane = {
                AnimatedPane1(Modifier.preferredWidth(480.dp)) {
                    SelectorConfigurationPane(
                        state = state.configurationState,
                        Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
                        contentPadding = paddingValues,
                    )
                }
            },
            detailPane = {
                AnimatedPane1 {
                    SelectorTestPane(
                        state.testState,
                        onViewEpisode = {
                            state.testState.viewEpisode(it)
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Extra)
                        },
                        Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
                        paddingValues,
                    )
                }
            },
            Modifier.materialWindowMarginPadding(),
            extraPane = {
                AnimatedPane1 {
                    SelectorEpisodePane(
                        state.episodeState,
                        SelectorEpisodePaneLayout.calculate(navigator.scaffoldValue),
                        configurationContent = {
                            SelectorEpisodePaneDefaults.ConfigurationContent(
                                state.configurationState,
                                contentPadding = it,
                            )
                        },
                        Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
                        paddingValues,
                    )
                }
            },
        )
    }
}
