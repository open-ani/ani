package me.him188.ani.app.ui.settings.tabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldDialog

@Composable
private fun PreviewTab(
    content: @Composable SettingsScope.() -> Unit,
) {
    ProvideCompositionLocalsForPreview {
        SettingsTab {
            content()
        }
    }
}

@Preview
@Composable
private fun PreviewPreferencePage() {
    ProvideCompositionLocalsForPreview {
        SettingsPage()
    }
}
@Preview
@Composable
private fun PreviewPreferenceScope() {
    ProvideCompositionLocalsForPreview {
        SettingsTab {
            SwitchItem(
                checked = true,
                onCheckedChange = {},
                title = {
                    Text("Test")
                },
                description = {
                    Text(text = "Test description")
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTextFieldDialog() {
    PreviewTab {
        TextFieldDialog(
            onDismissRequest = {},
            onConfirm = {},
            title = { Text(text = "编辑") },
            description = { Text(LoremIpsum(20).values.first()) }
        ) {
            OutlinedTextField(
                value = "test",
                onValueChange = {},
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}