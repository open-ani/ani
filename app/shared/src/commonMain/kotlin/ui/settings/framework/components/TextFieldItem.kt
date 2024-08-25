package me.him188.ani.app.ui.settings.framework.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.him188.ani.app.ui.foundation.effects.defaultFocus
import me.him188.ani.app.ui.foundation.effects.onKey
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor


/**
 * @param sanitizeValue 每当用户输入时调用, 可以清除首尾空格等
 * @param onValueChangeCompleted 当用户点击对话框的 "确认" 时调用
 */
@SettingsDsl
@Composable
fun SettingsScope.TextFieldItem(
    value: String,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable ((value: String) -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    onValueChangeCompleted: (value: String) -> Unit = {},
    inverseTitleDescription: Boolean = false,
    isErrorProvider: (value: String) -> Boolean = { false }, // calculated in a derivedState
    sanitizeValue: (value: String) -> String = { it },
    textFieldDescription: @Composable ((value: String) -> Unit)? = description,
    exposedItem: @Composable (value: String) -> Unit = { Text(it) },
    extra: @Composable ColumnScope.(editingValue: MutableState<String>) -> Unit = {}
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Item(
        modifier.clickable(onClick = { showDialog = true }),
        icon = icon,
    ) {
        Row(
            Modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 保存了的值
            val valueText = @Composable {
                if (placeholder != null && value.isEmpty()) {
                    placeholder()
                } else {
                    exposedItem(value)
                }
            }
            ItemHeader(
                title = {
                    if (inverseTitleDescription) {
                        valueText()
                    } else {
                        title()
                    }

                },
                description = {
                    if (inverseTitleDescription) {
                        title()
                    } else {
                        valueText()
                    }
                },
                Modifier.weight(1f),
            )

            IconButton({ showDialog = true }) {
                Icon(Icons.Rounded.Edit, "编辑", tint = MaterialTheme.colorScheme.primary)
            }

            if (showDialog) {
                // 正在编辑的值
                val editingValueState = rememberSaveable(value) {
                    mutableStateOf(value)
                }
                var editingValue by editingValueState
                val error by remember(isErrorProvider) {
                    derivedStateOf {
                        isErrorProvider(editingValue)
                    }
                }
                val onConfirm = remember(onValueChangeCompleted) {
                    {
                        onValueChangeCompleted(editingValue)
                        showDialog = false
                    }
                }

                TextFieldDialog(
                    onDismissRequest = { showDialog = false },
                    onConfirm = onConfirm,
                    title = title,
                    confirmEnabled = !error,
                    description = { textFieldDescription?.invoke(editingValue) },
                    extra = { extra(editingValueState) },
                ) {
                    OutlinedTextField(
                        value = editingValue,
                        onValueChange = { editingValue = sanitizeValue(it) },
                        shape = MaterialTheme.shapes.medium,
                        keyboardActions = KeyboardActions {
                            if (!error) {
                                onConfirm()
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth()
                            .defaultFocus()
                            .onKey(Key.Enter) {
                                if (!error) {
                                    onConfirm()
                                }
                            },
                        isError = error,
                    )
                }
            }
        }
    }
}


/**
 * [TextFieldItem] 使用
 */
@Composable
internal fun SettingsScope.TextFieldDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: @Composable () -> Unit,
    confirmEnabled: Boolean = true,
    description: @Composable (() -> Unit)? = null,
    extra: @Composable (ColumnScope.() -> Unit) = {},
    textField: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        title()
                    }
                }

                Row {
                    textField()
                }

                extra()

                ProvideTextStyleContentColor(
                    MaterialTheme.typography.labelMedium,
                    LocalContentColor.current.copy(labelAlpha),
                ) {
                    description?.let {
                        Row(Modifier.padding(horizontal = 8.dp)) {
                            it()
                        }
                    }
                }

                Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismissRequest) { Text("取消") }

                    Button(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}
