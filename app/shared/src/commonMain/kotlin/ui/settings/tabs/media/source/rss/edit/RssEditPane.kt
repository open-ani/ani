package me.him188.ani.app.ui.settings.tabs.media.source.rss.edit

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourceState

@Composable
fun RssEditPane(
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
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
        ) {
            val headlineStyle = computeRssHeadlineStyle()
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    state.displayIconUrl,
                    contentDescription = null,
                    Modifier
                        .padding(top = headlineStyle.imageTitleSpacing)
                        .size(headlineStyle.imageSize)
                        .clip(MaterialTheme.shapes.medium),
                    error = if (LocalIsPreviewing.current) rememberVectorPainter(Icons.Outlined.DisplaySettings) else null,
                )

                Text(
                    state.displayName,
                    Modifier
                        .padding(top = headlineStyle.imageTitleSpacing)
                        .padding(bottom = headlineStyle.imageTitleSpacing),
                    style = headlineStyle.titleTextStyle,
                    textAlign = TextAlign.Center,
                )
            }

            val textFieldShape = MaterialTheme.shapes.medium
            Column(
                Modifier.focusGroup()
                    .fillMaxHeight()
                    .padding(vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (isInDebugMode()) {
                        OutlinedTextField(
                            state.instanceId, { },
                            Modifier
                                .fillMaxWidth(),
                            label = { Text("[debug] instanceId") },
                            placeholder = { Text("设置显示在列表中的名称") },
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
    }
}

@Immutable
private class RssHeadlineStyle(
    val imageSize: DpSize,
    val titleTextStyle: TextStyle,
    val imageTitleSpacing: Dp,
)

@Composable
private fun computeRssHeadlineStyle(): RssHeadlineStyle {
    return when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> {
            RssHeadlineStyle(
                imageSize = DpSize(96.dp, 96.dp),
                titleTextStyle = MaterialTheme.typography.headlineMedium,
                imageTitleSpacing = 12.dp,
            )
        }

        // MEDIUM, EXPANDED for now,
        // and LARGE in the future
        else -> {
            RssHeadlineStyle(
                imageSize = DpSize(128.dp, 128.dp),
                titleTextStyle = MaterialTheme.typography.displaySmall,
                imageTitleSpacing = 20.dp,
            )
        }
    }
}

