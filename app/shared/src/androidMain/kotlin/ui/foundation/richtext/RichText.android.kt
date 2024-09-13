package me.him188.ani.app.ui.foundation.richtext

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.richtext.RichText
import me.him188.ani.app.ui.richtext.UIRichElement

@Preview
@Composable
private fun PreviewRichText() {
    ProvideCompositionLocalsForPreview {
        RichText(
            elements = listOf(
                UIRichElement.AnnotatedText(
                    slice = listOf(
                        UIRichElement.Annotated.Text("hello", 16f),
                        UIRichElement.Annotated.Text("my italic content", 16f, italic = true),
                        UIRichElement.Annotated.Text("my bold content", 16f, bold = true),
                        UIRichElement.Annotated.Text("my underline content", 16f, underline = true),
                        UIRichElement.Annotated.Text("my strikethrough content", 16f, strikethrough = true),
                        UIRichElement.Annotated.Text(
                            "my combined content",
                            16f,
                            bold = true,
                            underline = true,
                            strikethrough = true,
                        ),
                        UIRichElement.Annotated.Text("have link", 16f, url = "https://localhost"),
                        UIRichElement.Annotated.Text(
                            "have link combined",
                            16f,
                            bold = true,
                            strikethrough = true,
                            url = "https://localhost",
                        ),
                        UIRichElement.Annotated.Sticker(
                            "sticker_1",
                            null,
                            "",
                        ),
                    ),
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}