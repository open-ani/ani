package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.ktor.http.decodeURLQueryComponent
import me.him188.ani.app.ui.foundation.interaction.onRightClickIfSupported
import me.him188.ani.app.ui.foundation.widgets.LocalToaster

@Composable
@Suppress("UnusedReceiverParameter")
fun RssTestPaneDefaults.OverviewTab(
    result: RssTestResult.Success,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
) {
    Column(modifier) {
        RssOverviewCard(
            result,
            contentPadding = PaddingValues(16.dp),
            state = state,
        )
    }
}


@Composable
fun RssOverviewCard(
    result: RssTestResult.Success,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    state: LazyGridState = rememberLazyGridState(),
) {
    val channel = result.channel
    val encodedUrl = result.encodedUrl
    Card(modifier, colors = colors, shape = MaterialTheme.shapes.large) {
        val toaster = LocalToaster.current
        val clipboard = LocalClipboardManager.current
        val copy = { str: String ->
            clipboard.setText(AnnotatedString(str))
            toaster.toast("已复制到剪贴板")
        }

        fun Modifier.copyable(value: () -> String): Modifier {
            val func = { copy(value()) }
            return combinedClickable(
                onLongClick = func,
                onLongClickLabel = "复制",
                onClick = func, // no-op
                onClickLabel = "复制",
            ).onRightClickIfSupported(onClick = func)
        }

        val listItemColors = ListItemDefaults.colors(
            containerColor = colors.containerColor,
        )

        LazyVerticalGrid(
            GridCells.Adaptive(minSize = 300.dp),
            state = state,
            contentPadding = contentPadding,
        ) {
            item {
                ListItem(
                    headlineContent = { Text("Encoded Query URL") },
                    Modifier
                        .copyable { encodedUrl },
                    supportingContent = { Text(encodedUrl) },
                    colors = listItemColors,
                )
            }
            item {
                val url = encodedUrl.runCatching { decodeURLQueryComponent() }
                    .getOrElse { encodedUrl }
                ListItem(
                    headlineContent = { Text("Query URL") },
                    Modifier
                        .copyable { url },
                    supportingContent = { Text(url) },
                    colors = listItemColors,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Title") },
                    Modifier.copyable { channel.title },
                    supportingContent = { Text(channel.title) },
                    colors = listItemColors,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Description") },
                    Modifier.copyable { channel.description },
                    supportingContent = { Text(channel.description) },
                    colors = listItemColors,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Link") },
                    Modifier
                        .copyable { channel.link },
                    supportingContent = { Text(channel.link) },
                    colors = listItemColors,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("RSS Item Count") },
                    Modifier,
                    supportingContent = { SelectionContainer { Text(channel.items.size.toString()) } },
                    colors = listItemColors,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Parsed Media Count") },
                    Modifier,
                    supportingContent = { SelectionContainer { Text(result.mediaList.size.toString()) } },
                    colors = listItemColors,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ListItem(
                    headlineContent = { Text("原始 XML") },
                    Modifier.copyable { result.originString },
                    supportingContent = {
                        if (result.origin == null) {
                            Text("不可用")
                        } else {
                            OutlinedTextField(
                                value = result.originString,
                                onValueChange = {},
                                Modifier.padding(vertical = 8.dp),
                                readOnly = true,
                                minLines = 1,
                                maxLines = 8,
                            )
                        }
                    },
                    colors = listItemColors,
                )
            }
        }
    }
}