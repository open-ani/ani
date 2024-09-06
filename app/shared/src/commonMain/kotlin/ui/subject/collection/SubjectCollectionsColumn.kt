package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.components.AiringLabel
import me.him188.ani.app.ui.subject.collection.components.AiringLabelState
import me.him188.ani.app.ui.subject.details.components.COVER_WIDTH_TO_HEIGHT_RATIO

private inline val spacedBy get() = 16.dp

@Stable
class SubjectCollectionColumnState(
    cachedData: State<List<SubjectCollection>>,
    hasMore: State<Boolean>,
    isKnownAuthorizedAndEmpty: State<Boolean>,
    private val onRequestMore: suspend () -> Unit,
    backgroundScope: CoroutineScope,
) {
    private val requestMoreTasker = MonoTasker(backgroundScope)

    val cachedData: List<SubjectCollection> by cachedData
    val hasMore by hasMore

    /**
     * 如果未登录, 此属性会一直未 false
     */
    val isKnownAuthorizedAndEmpty by isKnownAuthorizedAndEmpty

    internal val gridState = LazyGridState()

    fun requestMore() {
        if (requestMoreTasker.isRunning) return
        requestMoreTasker.launch {
            onRequestMore()
        }
    }
}

/**
 * Lazy column of [item]s, designed for My Collections.
 *
 * @param item composes each item. See [SubjectCollection]
 * @param onEmpty content to be displayed when [LazyDataCache.cachedDataFlow] is empty.
 * @param contentPadding 要求该 column 的内容必须保持的 padding.
 * [SubjectCollectionsColumn] 将会允许元素渲染到这些区域, 但会在列表首尾添加 padding.
 * 这样可以让列表渲染到 bottom bar 的下面 (bottom bar 设置 alpha 0.97), 而用户又能正常地滑动到列表尾部并完整显示最后一个元素.
 */
@Composable
fun SubjectCollectionsColumn(
    state: SubjectCollectionColumnState,
    item: @Composable (item: SubjectCollection) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enableAnimation: Boolean = true,
    allowProgressIndicator: Boolean = true,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPaddingState by rememberUpdatedState(contentPadding)
    val gridContentPadding by remember(layoutDirection) {
        derivedStateOf {
            PaddingValues(
                // 每两个 item 之间有 spacedBy dp, 这里再上补充 contentPadding 要求的高度, 这样顶部的总留空就是 contentPadding 要求的高度
                // 这个高度是可以滚到上面的, 所以它
                top = (contentPaddingState.calculateTopPadding() + spacedBy).coerceAtLeast(0.dp),
                bottom = (contentPaddingState.calculateBottomPadding() + spacedBy).coerceAtLeast(0.dp),
                start = contentPaddingState.calculateStartPadding(layoutDirection),
                end = contentPaddingState.calculateEndPadding(layoutDirection),
            )
        }
    }

    LazyVerticalGrid(
        GridCells.Adaptive(360.dp),
        modifier.padding(horizontal = 12.dp).padding(vertical = 0.dp),
        state.gridState,
        verticalArrangement = Arrangement.spacedBy(spacedBy),
        horizontalArrangement = Arrangement.spacedBy(spacedBy),
        contentPadding = contentPaddingState,
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {}

        items(state.cachedData, key = { it.subjectId }) { collection ->
            Box(Modifier.ifThen(enableAnimation) { animateItemPlacement() }) {
                item(collection)
            }
        }

        if (state.hasMore) {
            item("dummy loader", span = { GridItemSpan(maxLineSpan) }) {
                if (allowProgressIndicator) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.requestMore()
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {}
    }
}

/**
 * 追番列表的一个条目卡片
 *
 * @param onClick on clicking this card (background)
 */
@Composable
fun SubjectCollectionItem(
    item: SubjectCollection,
    editableSubjectCollectionTypeState: EditableSubjectCollectionTypeState,
    onClick: () -> Unit,
    onShowEpisodeList: () -> Unit,
    playButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 148.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
) {
    Card(
        onClick,
        modifier.clip(shape).fillMaxWidth().height(height),
        shape = shape,
    ) {
        Row(Modifier.weight(1f, fill = false)) {
            AsyncImage(
                item.info.imageCommon,
                contentDescription = null,
                modifier = Modifier
                    .height(height).width(height * COVER_WIDTH_TO_HEIGHT_RATIO),
                contentScale = ContentScale.Crop,
            )

            Box(Modifier.weight(1f)) {
                SubjectCollectionItemContent(
                    item = item,
                    editableSubjectCollectionTypeState = editableSubjectCollectionTypeState,
                    onShowEpisodeList = onShowEpisodeList,
                    playButton = playButton,
                    Modifier.padding(start = 12.dp).fillMaxSize(),
                )
            }
        }
    }
}

/**
 * 追番列表的一个条目卡片的内容
 */
@Composable
private fun SubjectCollectionItemContent(
    item: SubjectCollection,
    editableSubjectCollectionTypeState: EditableSubjectCollectionTypeState,
    onShowEpisodeList: () -> Unit,
    playButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        // 标题和右上角菜单
        Row(
            Modifier.fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                item.displayName,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )

            Box {
                IconButton(
                    { editableSubjectCollectionTypeState.showDropdown = true },
                    Modifier.fillMaxHeight().padding(),
                ) {
                    Icon(Icons.Outlined.MoreVert, null, Modifier.size(24.dp))
                }

                EditCollectionTypeDropDown(editableSubjectCollectionTypeState)
            }
        }

        Row(
            Modifier.padding(top = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 连载至第 28 话 · 全 34 话
            AiringLabel(
                remember(item) {
                    AiringLabelState(stateOf(item.airingInfo), stateOf(item.progressInfo))
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.weight(1f))


        Row(
            Modifier
                .padding(vertical = 12.dp)
                .padding(horizontal = 12.dp)
                .align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onShowEpisodeList) {
                Text("选集")
            }

            Box(Modifier.width(IntrinsicSize.Min)) { playButton() }
        }
    }
}


