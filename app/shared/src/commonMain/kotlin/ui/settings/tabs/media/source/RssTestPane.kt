package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.source.media.source.RssMediaSourceEngine
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.saveable.protobufSaver
import kotlin.coroutines.CoroutineContext

@Serializable
sealed class RssTestData {
    data class Test(
        val s: String
    )

    companion object {
        val Saver = protobufSaver(serializer())
    }
}

class RssMediaSourceTester(
    private val mediaSourceId: String,
    engine: Flow<RssMediaSourceEngine>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private val keyword: MutableStateFlow<String?> = MutableStateFlow(null)
    private val results = combine(engine.shareInBackground(), keyword) { engine, keyword ->
        if (keyword == null) {
            emptyList()
        } else {
            engine.search(keyword)
        }
    }

    fun setKeyword(value: String) {
        keyword.value = value
    }
}

@Stable
class RssTestPaneState(
//    private val tester: RssMediaSourceTester,
) {
    var searchKeywordPlaceholder = SampleKeywords.random()
    var searchKeyword: String by mutableStateOf(searchKeywordPlaceholder)

    fun randomKeyword() {
        val newRandom = SampleKeywords.random()
        searchKeywordPlaceholder = newRandom
        searchKeyword = newRandom
    }

    fun startSearch() {
        val finalKeyword = searchKeyword.ifEmpty { searchKeywordPlaceholder }
//        tester
    }
}

@Stable
private val SampleKeywords
    get() = listOf(
        "败犬女主太多了！",
        "白箱",
        "CLANNAD",
        "轻音少女",
        "战姬绝唱",
        "凉宫春日的忧郁",
        "樱 Trick",
        "命运石之门",
    )

@Composable
fun RssTestPane(
    state: RssTestPaneState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier
            .padding(contentPadding),
    ) {
        Text("测试数据源", style = MaterialTheme.typography.headlineSmall)

        Card(
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(all = 16.dp).focusGroup()) {
                LazyVerticalGrid(
                    GridCells.Adaptive(minSize = 300.dp),
                ) {
                    item {
                        TextField(
                            value = state.searchKeyword,
                            onValueChange = { state.searchKeyword = it.trim() },
                            Modifier.animateItem().fillMaxWidth(),
                            label = { Text("关键词") },
                            placeholder = {
                                Text(
                                    state.searchKeywordPlaceholder,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { state.searchKeyword = "" }) {
                                    Icon(Icons.Rounded.RestartAlt, contentDescription = "随机")
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        )
                    }
                }
            }
        }

        Row(Modifier.padding(top = 20.dp, bottom = 12.dp)) {
            Text("查询结果", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
