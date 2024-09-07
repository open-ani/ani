package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.data.source.media.fetch.updateMediaSourceArguments
import me.him188.ani.app.data.source.media.source.RssMediaSource
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.navigation.LocalBackHandler
import me.him188.ani.app.platform.navigation.BackHandler
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.datasources.api.source.deserializeArgumentsOrNull
import me.him188.ani.utils.platform.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditRssMediaSourceViewModel(
    initialMode: EditMediaSourceMode
) : AbstractViewModel(), KoinComponent {
    private val mediaSourceManager: MediaSourceManager by inject()

    private val mode: MutableStateFlow<EditMediaSourceMode> = MutableStateFlow(initialMode)

    val onSave: suspend (String, RssMediaSourceArguments) -> Unit = { instanceId, arguments ->
        mediaSourceManager.updateMediaSourceArguments(
            instanceId,
            RssMediaSourceArguments.serializer(),
            arguments,
        )
    }

    val state: Flow<EditRssMediaSourceState> = mode.transformLatest { mode ->
        when (mode) {
            EditMediaSourceMode.Add -> {
                val instanceId = Uuid.randomString()
                emit(
                    EditRssMediaSourceState(
                        arguments = RssMediaSourceArguments.Default,
                        editMediaSourceMode = mode,
                        instanceId = instanceId,
                        onSave = { onSave(instanceId, it) },
                        backgroundScope,
                    ),
                )
            }

            is EditMediaSourceMode.Edit -> {
                val instanceId = mode.instanceId
                emitAll(
                    mediaSourceManager.instanceConfigFlow(instanceId).map { config ->
                        EditRssMediaSourceState(
                            arguments = config.deserializeArgumentsOrNull(RssMediaSourceArguments.serializer())
                                ?: RssMediaSourceArguments.Default,
                            editMediaSourceMode = mode,
                            instanceId = instanceId,
                            onSave = { onSave(instanceId, it) },
                            backgroundScope,
                        )
                    },
                )
            }
        }
    }

    val testState: RssTestPaneState = RssTestPaneState()
}

/**
 * @see RssMediaSource
 */
@Stable
class EditRssMediaSourceState(
    val arguments: RssMediaSourceArguments,
    val editMediaSourceMode: EditMediaSourceMode,
    instanceId: String,
    private val onSave: suspend (RssMediaSourceArguments) -> Unit,
    backgroundScope: CoroutineScope,
) {
    var instanceId by mutableStateOf(instanceId)

    var displayName by mutableStateOf(arguments.name)
    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl by mutableStateOf(arguments.iconUrl)

    val displayIconUrl by derivedStateOf {
        iconUrl.ifBlank { RssMediaSourceArguments.DEFAULT_ICON_URL }
    }

    private var _searchUrl by mutableStateOf(arguments.searchUrl)
    var searchUrl
        get() = _searchUrl
        set(value) {
            _searchUrl = value
            everEditedSearchUrl = true
        }
    private var everEditedSearchUrl by mutableStateOf(false)
    val searchUrlIsError by derivedStateOf { everEditedSearchUrl && searchUrl.isBlank() }

    var dismissChanges by mutableStateOf(false)
    val isChanged by derivedStateOf {
        displayName != arguments.name || iconUrl != arguments.iconUrl || searchUrl != arguments.searchUrl
    }

    private val saveTasker = MonoTasker(backgroundScope)

    fun save(): Boolean {
        if (searchUrl.isBlank()) {
            everEditedSearchUrl = true
            return false
        }
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

    Scaffold(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopAppBar(
                title = {
                    when (state.editMediaSourceMode) {
                        is EditMediaSourceMode.Edit -> Text(state.displayName)
                        EditMediaSourceMode.Add -> Text("新建 RSS 数据源")
                    }
                },
                navigationIcon = { TopAppBarGoBackButton() },
                actions = {
                    IconButton({ state.save() }, enabled = state.isChanged) {
                        Icon(Icons.Rounded.Save, contentDescription = "保存")
                    }
                },
            )
        },
    ) { paddingValues ->
        ListDetailPaneScaffold(
            navigator.scaffoldDirective,
            navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    RssEditPane(
                        state,
                        onClickTest = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) },
                        showTestButton = navigator.scaffoldValue.primary == PaneAdaptedValue.Hidden,
                        Modifier
                            .fillMaxSize()
                            .padding(all = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        paddingValues,
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    RssTestPane(
                        testState,
                        Modifier
                            .fillMaxSize()
                            .padding(all = 16.dp),
                        paddingValues,
                    )
                }
            },
        )
    }
}

@Composable
private fun RssEditPane(
    state: EditRssMediaSourceState,
    onClickTest: () -> Unit,
    showTestButton: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier
            .padding(contentPadding),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                state.displayIconUrl,
                contentDescription = null,
                Modifier
                    .padding(top = 20.dp)
                    .size(128.dp)
                    .clip(MaterialTheme.shapes.medium),
                error = if (LocalIsPreviewing.current) rememberVectorPainter(Icons.Outlined.DisplaySettings) else null,
            )

            Text(
                state.displayName,
                Modifier
                    .padding(top = 20.dp)
                    .padding(bottom = 20.dp),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            Modifier.weight(1f),
//            shape = MaterialTheme.shapes.extraLarge,
        ) {
            val textFieldShape = MaterialTheme.shapes.medium
            Column(
                Modifier.focusGroup()
                    .fillMaxHeight()
                    .padding(vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (isInDebugMode()) {
                        OutlinedTextField(
                            state.instanceId, { state.instanceId = it.trim() },
                            Modifier
                                .fillMaxWidth(),
                            label = { Text("名称") },
                            placeholder = { Text("设置显示在列表中的名称") },
                            isError = state.displayNameIsError,
                            supportingText = { Text("必填") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            readOnly = true,
                            shape = textFieldShape,
                        )
                    }

                    OutlinedTextField(
                        state.displayName, { state.displayName = it.trim() },
                        Modifier
                            .fillMaxWidth(),
                        label = { Text("名称*") },
                        maxLines = 1,
                        placeholder = { Text("设置显示在列表中的名称") },
                        isError = state.displayNameIsError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = textFieldShape,
                    )
                    OutlinedTextField(
                        state.iconUrl, { state.iconUrl = it.trim() },
                        Modifier
                            .fillMaxWidth(),
                        label = { Text("图标链接") },
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = textFieldShape,
                    )
                }

                Row(Modifier.padding(top = 20.dp, bottom = 12.dp)) {
                    ProvideTextStyleContentColor(
                        MaterialTheme.typography.titleMedium,
                        MaterialTheme.colorScheme.primary,
                    ) {
                        Text("查询设置")
                    }
                }

                Column(Modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedTextField(
                        state.searchUrl, { state.searchUrl = it },
                        Modifier.fillMaxWidth(),
                        label = { Text("搜索链接*") },
                        placeholder = {
                            Text(
                                "示例:  https://acg.rip/page/{page}.xml?term={keyword}",
                                color = MaterialTheme.colorScheme.outline,
                                softWrap = false,
                            )
                        },
                        supportingText = {
                            Text(
                                """
                                    替换规则: 
                                    {keyword} 替换为条目 (番剧) 名称
                                    {page} 替换为页码, 如果不需要分页则忽略
                                """.trimIndent(),
                            )
                        },
                        isError = state.searchUrlIsError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = textFieldShape,
                    )
                }
            }
        }

        if (showTestButton) {
            FilledTonalButton(
                onClick = onClickTest,
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text("测试")
            }
        }

        Button(
            onClick = { state.save() },
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = state.isChanged,
        ) {
            Text("保存")
        }
    }
}
