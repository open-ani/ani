package me.him188.ani.app.ui.settings.tabs.media.source.rss

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import me.him188.ani.app.data.source.media.source.RssMediaSource
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.data.source.media.source.RssSearchConfig
import me.him188.ani.app.platform.navigation.BackHandler
import me.him188.ani.app.ui.foundation.layout.AnimatedPane1
import me.him188.ani.app.ui.foundation.layout.materialWindowMarginPadding
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.RssDetailPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.SideSheetPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.edit.RssEditPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPaneState

/**
 * 整个编辑 RSS 数据源页面的状态. 对于测试部分: [RssTestPaneState]
 *
 * @see RssMediaSource
 */
@Stable
class EditRssMediaSourceState(
    argumentsStorage: SaveableStorage<RssMediaSourceArguments>,
    val instanceId: String,
) {
    private val arguments by argumentsStorage.containerState
    val isLoading by derivedStateOf { arguments == null }
    val isSaving by argumentsStorage.isSavingState

    var displayName by argumentsStorage.prop(
        RssMediaSourceArguments::name, { copy(name = it) },
        "",
    )

    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl by argumentsStorage.prop(
        RssMediaSourceArguments::iconUrl, { copy(iconUrl = it) },
        "",
    )
    val displayIconUrl by derivedStateOf {
        iconUrl.ifBlank { RssMediaSourceArguments.DEFAULT_ICON_URL }
    }

    var searchUrl by argumentsStorage.prop(
        { it.searchConfig.searchUrl }, { copy(searchConfig = searchConfig.copy(searchUrl = it)) },
        "",
    )
    val searchUrlIsError by derivedStateOf { searchUrl.isBlank() }

    var filterByEpisodeSort by argumentsStorage.prop(
        { it.searchConfig.filterByEpisodeSort }, { copy(searchConfig = searchConfig.copy(filterByEpisodeSort = it)) },
        true,
    )
    var filterBySubjectName by argumentsStorage.prop(
        { it.searchConfig.filterBySubjectName }, { copy(searchConfig = searchConfig.copy(filterBySubjectName = it)) },
        true,
    )

    val searchConfig by derivedStateOf {
        RssSearchConfig(
            searchUrl = searchUrl,
            filterByEpisodeSort = filterByEpisodeSort,
            filterBySubjectName = filterBySubjectName,
        )
    }
}

@Composable
fun EditRssMediaSourcePage(
    viewModel: EditRssMediaSourceViewModel,
    modifier: Modifier = Modifier,
) {
    viewModel.state.collectAsStateWithLifecycle(null).value?.let {
        EditRssMediaSourcePage(it, viewModel.testState, modifier)
    }
}

@Composable
fun EditRssMediaSourcePage(
    state: EditRssMediaSourceState,
    testState: RssTestPaneState,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<Nothing> = rememberListDetailPaneScaffoldNavigator()
) {
    LaunchedEffect(Unit) {
        testState.observeChangeLoop()
    }

    Scaffold(
        modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(state.displayName.ifEmpty { "新建数据源" })
                },
                navigationIcon = { TopAppBarGoBackButton() },
            )
        },
    ) { paddingValues ->
        BackHandler(navigator.canNavigateBack()) {
            navigator.navigateBack()
        }

        ListDetailPaneScaffold(
            navigator.scaffoldDirective,
            navigator.scaffoldValue.let { value ->
                if (value.tertiary == PaneAdaptedValue.Expanded && value.secondary == PaneAdaptedValue.Expanded) {
                    // 手机上三级导航, PC 上将 detail pane (test) 移动到左边, 隐藏 list (edit)
                    ThreePaneScaffoldValue(
                        primary = PaneAdaptedValue.Expanded, // detail
                        secondary = PaneAdaptedValue.Hidden, // list
                        tertiary = PaneAdaptedValue.Expanded,
                    )
                } else {
                    value
                }
            },
            listPane = {
                AnimatedPane1 {
                    RssEditPane(
                        state = state,
                        onClickTest = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) },
                        showTestButton = navigator.scaffoldValue.primary == PaneAdaptedValue.Hidden,
                        Modifier.fillMaxSize(),
                        contentPadding = paddingValues,
                    )
                }
            },
            detailPane = {
                AnimatedPane1 {
                    RssTestPane(
                        testState,
                        { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) },
                        Modifier.fillMaxSize(),
                        contentPadding = paddingValues,
                    )
                }
            },
            Modifier.materialWindowMarginPadding(),
            extraPane = {
                AnimatedPane1 {
                    Crossfade(testState.viewingItem) { item ->
                        item ?: return@Crossfade
                        if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT) {
                            SideSheetPane(
                                onClose = { navigator.navigateBack() },
                                Modifier.padding(paddingValues),
                            ) {
                                RssDetailPane(
                                    item,
                                    Modifier
                                        .fillMaxSize(),
                                )
                            }
                        } else {
                            RssDetailPane(
                                item,
                                Modifier
                                    .fillMaxSize(),
                                contentPadding = paddingValues,
                            )
                        }
                    }
                }
            },
        )
    }
}

