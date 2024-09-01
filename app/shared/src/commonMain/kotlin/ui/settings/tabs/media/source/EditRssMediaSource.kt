package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import me.him188.ani.app.ui.foundation.isInDebugMode
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
                        mediaSourceId = instanceId,
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
                            mediaSourceId = instanceId,
                            onSave = { onSave(instanceId, it) },
                            backgroundScope,
                        )
                    },
                )
            }
        }
    }
}

/**
 * @see RssMediaSource
 */
@Stable
class EditRssMediaSourceState(
    val arguments: RssMediaSourceArguments,
    val editMediaSourceMode: EditMediaSourceMode,
    mediaSourceId: String,
    private val onSave: suspend (RssMediaSourceArguments) -> Unit,
    backgroundScope: CoroutineScope,
) {
    var mediaSourceId by mutableStateOf(mediaSourceId)

    var displayName by mutableStateOf(arguments.name)
    val displayNameIsError by derivedStateOf { displayName.isBlank() }

    var iconUrl by mutableStateOf(arguments.iconUrl)
    var searchUrl by mutableStateOf(arguments.searchUrl)

    var dismissChanges by mutableStateOf(false)
    val isChanged by derivedStateOf {
        displayName != arguments.name || iconUrl != arguments.iconUrl || searchUrl != arguments.searchUrl
    }

    private val saveTasker = MonoTasker(backgroundScope)

    fun save() {
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
    }
}

@Composable
fun EditRssMediaSourcePage(
    viewModel: EditRssMediaSourceViewModel,
    modifier: Modifier = Modifier,
) {
    viewModel.state.collectAsStateWithLifecycle(null).value?.let {
        EditRssMediaSourcePage(it, modifier)
    }
}

@Composable
fun EditRssMediaSourcePage(
    state: EditRssMediaSourceState,
    modifier: Modifier = Modifier,
) {
    val backHandler = LocalBackHandler.current
    val confirmDiscardDialog = rememberConfirmDiscardChangeDialogState {
        state.dismissChanges = false
        backHandler.onBackPressed()
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
                    IconButton({ state.save() }) {
                        Icon(Icons.Rounded.Save, contentDescription = "保存")
                    }
                },
            )
        },
    ) { paddingValues ->
        Surface {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier.align(Alignment.TopCenter).fillMaxHeight().widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                        .padding(paddingValues),
                ) {
                    if (isInDebugMode()) {
                        ListItem(
                            headlineContent = { Text("ID") },
                            supportingContent = { Text(state.mediaSourceId) },
                        )
                    }
                    ListItem(
                        headlineContent = { Text("名称") },
                        supportingContent = {
                            OutlinedTextField(
                                state.displayName, { state.displayName = it.trim() },
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                placeholder = { Text("设置显示在列表中的名称") },
                                isError = state.displayNameIsError,
                                supportingText = { Text("必填") },
                            )
                        },
                    )
//            ListItem(
//                headlineContent = { Text("备注") },
//                supportingContent = {
//                    OutlinedTextField(
//                        state.description, { state.description = it },
//                        placeholder = { Text("可留空") },
//                    )
//                },
//            )
                    ListItem(
                        headlineContent = { Text("图标 URL") },
                        supportingContent = {
                            OutlinedTextField(
                                state.iconUrl, { state.iconUrl = it },
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                supportingText = { Text("可留空") },
                            )
                        },
                    )
                    ListItem(
                        headlineContent = { Text("查询网址") },
                        supportingContent = {
                            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                                    Text(
                                        """
                                    替换规则: 
                                    {keyword} 替换为条目 (番剧) 名称
                                    {page} 替换为页码, 如果不需要分页则忽略
                                """.trimIndent(),
                                    )
                                }
                                OutlinedTextField(
                                    state.searchUrl, { state.searchUrl = it },
                                    Modifier.fillMaxWidth().padding(top = 8.dp),
                                    placeholder = { Text("例如  https://acg.rip/page/{page}.xml?term={keyword}") },
                                    supportingText = { Text("必填") },
                                )
                            }
                        },
                    )

                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                    Button(
                        onClick = { state.save() },
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        enabled = state.isChanged,
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}
