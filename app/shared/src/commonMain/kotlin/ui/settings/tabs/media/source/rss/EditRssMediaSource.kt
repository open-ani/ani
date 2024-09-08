package me.him188.ani.app.ui.settings.tabs.media.source.rss

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.him188.ani.app.data.source.media.source.RssMediaSource
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.platform.navigation.BackHandler
import me.him188.ani.app.ui.foundation.layout.materialWindowMarginPadding
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.RssDetailPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.SideSheetPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.edit.RssEditPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPaneState


/**
 * @see RssMediaSource
 */
@Stable
class EditRssMediaSourceState(
    argumentsState: State<RssMediaSourceArguments?>,
    val instanceId: String,
    /**
     * 必须立即反映到 [argumentsState]
     */
    private val onSave: (RssMediaSourceArguments) -> Unit,
    isSavingState: State<Boolean>,
) {
    private val arguments by argumentsState
    val isLoading by derivedStateOf { arguments == null }
    val isSaving by isSavingState

    var displayName
        get() = arguments?.name ?: ""
        set(value) {
            val arguments = arguments ?: return
            save(arguments.copy(name = value))
        }

    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl
        get() = arguments?.iconUrl ?: ""
        set(value) {
            val arguments = arguments ?: return
            save(arguments.copy(iconUrl = value))
        }
    val displayIconUrl by derivedStateOf {
        iconUrl.ifBlank { RssMediaSourceArguments.DEFAULT_ICON_URL }
    }

    var searchUrl
        get() = arguments?.searchUrl ?: ""
        set(value) {
            val arguments = arguments ?: return
            save(arguments.copy(searchUrl = value))
        }
    val searchUrlIsError by derivedStateOf { searchUrl.isBlank() }

    private fun save(newArguments: RssMediaSourceArguments): Boolean {
        onSave(newArguments)
        return true
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
                AnimatedPane {
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
                AnimatedPane {
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
                AnimatedPane {
                    Crossfade(testState.viewingItem) { item ->
                        item ?: return@Crossfade
                        if (navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded
                            || navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded
                        ) {
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
