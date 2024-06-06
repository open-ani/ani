package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.mediaSource.MediaSourceIcon
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.components.DropdownItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldItem
import me.him188.ani.datasources.api.source.BooleanParameter
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceParameter
import me.him188.ani.datasources.api.source.SimpleEnumParameter
import me.him188.ani.datasources.api.source.StringParameter
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.coroutines.CoroutineContext


sealed class EditType {
    data object Add : EditType()
    data class Edit(
        val instanceId: String,
    ) : EditType()
}

@Stable
class EditMediaSourceState(
    val info: MediaSourceInfo,
    persistedArguments: Flow<MediaSourceConfig>, // 加载会有延迟
    val editType: EditType,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext), Closeable {
    val arguments = info.parameters.list.map { param ->
        when (param) {
            is BooleanParameter -> BooleanArgumentState(param)
            is SimpleEnumParameter -> SimpleEnumArgumentState(param)
            is StringParameter -> StringArgumentState(param)
            else -> throw IllegalArgumentException("Unsupported parameter type: $param")
        }
    }

    private val persistLoader = persistedArguments.map { config ->
        for ((name, value) in config.arguments) {
            arguments.find { it.name == name }?.loadFromPersisted(value)
        }
    }.stateInBackground(null, started = SharingStarted.Eagerly)

    val isLoading = persistLoader.map { it != null }
    val hasError by derivedStateOf { arguments.any { it.isError } }

    override fun close() {
        backgroundScope.cancel()
    }
}

@Stable
sealed class ArgumentState(
    private val parameter: MediaSourceParameter<*>,
) {
    val name: String get() = parameter.name
    val description: String? get() = parameter.description

    abstract val isError: Boolean

    abstract fun loadFromPersisted(value: String?)

    abstract fun toPersisted(): String?
}

@Stable
class StringArgumentState(
    val parameter: StringParameter,
) : ArgumentState(parameter) {
    var value: String by mutableStateOf(parameter.default)
    override val isError: Boolean by derivedStateOf { !parameter.validate(value) }

    override fun loadFromPersisted(value: String?) {
        this.value = value ?: ""
    }

    override fun toPersisted(): String = value
}

@Stable
class BooleanArgumentState(
    private val origin: BooleanParameter,
) : ArgumentState(origin) {
    var value: Boolean by mutableStateOf(origin.default)
    override val isError: Boolean get() = false

    override fun loadFromPersisted(value: String?) {
        this.value = value?.toBooleanStrictOrNull() ?: origin.default
    }

    override fun toPersisted() = value.toString()
}

@Stable
class SimpleEnumArgumentState(
    private val origin: SimpleEnumParameter,
) : ArgumentState(origin) {
    val options get() = origin.oneOf
    var value: String by mutableStateOf(origin.default)
    override val isError: Boolean by derivedStateOf { value !in origin.oneOf }

    override fun loadFromPersisted(value: String?) {
        this.value = value.takeIf { it in origin.oneOf } ?: origin.default
    }

    override fun toPersisted() = value
}

@Composable
internal fun EditMediaSourceLayout(
    state: EditMediaSourceState,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by state.isLoading.collectAsStateWithLifecycle(true)
    RichDialogLayout(
        title = {
            when (state.editType) {
                EditType.Add -> Text("添加数据源")
                is EditType.Edit -> Text("编辑数据源")
            }
        },
        description = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.clip(MaterialTheme.shapes.extraSmall).size(24.dp)) {
                    MediaSourceIcon(state.info.mediaSourceId, Modifier.size(24.dp))
                }

                Text(state.info.name)
            }
        },
        buttons = {
            val canSave by remember(state) {
                derivedStateOf {
//                    !isLoading && todo 不知道为什么监听不到 isLoading, 但加载速度反正很快
                    !state.hasError
                }
            }
            TextButton(onDismissRequest) {
                Text("取消")
            }
            when (state.editType) {
                EditType.Add -> Button(onConfirm, enabled = canSave) { Text("添加") }
                is EditType.Edit -> Button(onConfirm, enabled = canSave) { Text("保存") }
            }
        },
        modifier = modifier,
    ) {
        SettingsTab {
            for ((index, argument) in state.arguments.withIndex()) {
                if (index != 0) {
                    HorizontalDividerItem()
                }
                when (argument) {
                    is BooleanArgumentState -> {
                        SwitchItem(
                            checked = argument.value,
                            onCheckedChange = {
                                argument.value = it
                            },
                            title = { Text(argument.name) },
                            description = argument.description?.let { { Text(it) } },
                        )
                    }

                    is SimpleEnumArgumentState -> {
                        DropdownItem(
                            selected = { argument.value },
                            values = { argument.options },
                            itemText = { Text(it) },
                            onSelect = { argument.value = it },
                            title = { Text(argument.name) },
                            description = argument.description?.let { { Text(it) } },
                        )
                    }

                    is StringArgumentState -> {
                        TextFieldItem(
                            value = argument.value,
                            title = { Text(argument.name) },
                            description = argument.description?.let { { Text(it) } },
                            onValueChangeCompleted = {
                                argument.value = it
                            },
                            sanitizeValue = { argument.parameter.sanitize(it) },
                            isErrorProvider = {
                                !argument.parameter.validate(it)
                            },
                        )
                    }
                }
            }
        }
    }
}
