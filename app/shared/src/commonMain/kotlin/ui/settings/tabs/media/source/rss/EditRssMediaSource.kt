package me.him188.ani.app.ui.settings.tabs.media.source.rss

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.source.media.source.RssMediaSource
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.navigation.LocalBackHandler
import me.him188.ani.app.platform.navigation.BackHandler
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.layout.materialWindowMarginPadding
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.settings.tabs.media.source.ConfirmDiscardChangeDialog
import me.him188.ani.app.ui.settings.tabs.media.source.EditMediaSourceMode
import me.him188.ani.app.ui.settings.tabs.media.source.rememberConfirmDiscardChangeDialogState
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.RssDetailPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.edit.RssEditPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPane
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPaneState

/**
 * @see RssMediaSource
 */
@Stable
class EditRssMediaSourceState(
    val arguments: RssMediaSourceArguments,
    val editMediaSourceMode: EditMediaSourceMode,
    val instanceId: String,
    private val onSave: suspend (RssMediaSourceArguments) -> Unit,
    backgroundScope: CoroutineScope,
) {
    @Stable
    inner class AutoSaveableState<T>(initialValue: T) : MutableState<T> {
        private val delegate = mutableStateOf(initialValue)
        override var value: T
            get() = delegate.value
            set(value) {
                delegate.value = value
                save()
            }

        override fun component1(): T = value
        override fun component2(): (T) -> Unit = { value = it }
    }

    var displayName by AutoSaveableState(arguments.name)
    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl by AutoSaveableState(arguments.iconUrl)
    val displayIconUrl by derivedStateOf {
        iconUrl.ifBlank { RssMediaSourceArguments.DEFAULT_ICON_URL }
    }

    var searchUrl by AutoSaveableState(arguments.searchUrl)
    val searchUrlIsError by derivedStateOf { searchUrl.isBlank() }

    var dismissChanges by mutableStateOf(false)
    val isChanged by derivedStateOf {
        displayName != arguments.name || iconUrl != arguments.iconUrl || searchUrl != arguments.searchUrl
    }

    private val saveTasker = MonoTasker(backgroundScope)

    fun save(): Boolean {
        saveTasker.launch {
            onSave(
                RssMediaSourceArguments(
                    name = displayName,
                    iconUrl = iconUrl,
                    searchUrl = searchUrl,
                    description = "",
                ),
            )
        }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EditRssMediaSourcePage(
    state: EditRssMediaSourceState,
    testState: RssTestPaneState,
    modifier: Modifier = Modifier,
    navigator: ThreePaneScaffoldNavigator<Nothing> = rememberListDetailPaneScaffoldNavigator()
) {
    val backHandler = LocalBackHandler.current
    val confirmDiscardDialog = rememberConfirmDiscardChangeDialogState {
        state.dismissChanges = true
        backHandler.onBackPressed() // TODO: 这没用, 因为 composition 还没来得及更新
    }
    LaunchedEffect(true) {
        snapshotFlow { state.isChanged }.collect {
            state.dismissChanges = false
        }
    }

    ConfirmDiscardChangeDialog(confirmDiscardDialog)

    BackHandler(enabled = state.isChanged && !state.dismissChanges) {
        confirmDiscardDialog.show()
    }

    LaunchedEffect(Unit) {
        testState.observeChangeLoop()
    }

    Scaffold(
        modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    when (state.editMediaSourceMode) {
                        is EditMediaSourceMode.Edit -> Text(state.displayName)
                        EditMediaSourceMode.Add -> Text("新建 RSS 数据源")
                    }
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
                    Crossfade(testState.viewingItem) {
                        RssDetailPane(
                            it ?: return@Crossfade,
                            Modifier
                                .fillMaxSize(),
                            contentPadding = paddingValues,
                        )
                    }
                }
            },
        )
    }
}
