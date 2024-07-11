package me.him188.ani.app.ui.foundation.richtext

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Preview
@Composable
private fun PreviewCommentColumn() {
    ProvideCompositionLocalsForPreview {
        RichText(
            elements = listOf(
                UIRichElement.AnnotatedText(
                    slice = listOf(
                        UIRichElement.Annotated.Text("hello", 16),
                        UIRichElement.Annotated.Text("my italic content", 16, italic = true),
                        UIRichElement.Annotated.Text("my bold content", 16, bold = true),
                        UIRichElement.Annotated.Text("my underline content", 16, underline = true),
                        UIRichElement.Annotated.Text("my strikethrough content", 16, strikethrough = true),
                        UIRichElement.Annotated.Text(
                            "my combined content",
                            16,
                            bold = true,
                            underline = true,
                            strikethrough = true,
                        ),
                        UIRichElement.Annotated.Text("have link", 16, url = "https://localhost"),
                        UIRichElement.Annotated.Text(
                            "have link combined",
                            16,
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