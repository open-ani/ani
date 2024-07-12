package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.subject.episode.comments.bbcode.BBCodeView
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
fun EpisodeCommentColumn(
    episodeId: Int,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val viewModel = rememberViewModel { EpisodeCommentViewModel(episodeId, pullToRefreshState) }

    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle()
    val comments by viewModel.list.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.reloadComments()

        snapshotFlow { pullToRefreshState.isRefreshing }.collectLatest { refreshing ->
            if (!refreshing) return@collectLatest
            viewModel.reloadComments()
        }
    }

    Box(modifier = modifier.clipToBounds()) {
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection),
        ) {
            item { }
            itemsIndexed(comments, key = { _, item -> item.id }) { index, item ->
                Comment(
                    comment = item,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .padding(vertical = 12.dp),
                )
                if (index != comments.lastIndex) {
                    HorizontalDivider(
                        modifier = modifier.fillMaxWidth(),
                        color = DividerDefaults.color.stronglyWeaken(),
                    )
                }
            }
            if (hasMore) {
                item("dummy loader") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }

                    LaunchedEffect(true) { viewModel.loadMoreComments() }
                }
            }
        }
    }

}

private const val LOREM_IPSUM =
    "Ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet."

@Composable
fun Comment(
    comment: UiComment,
    modifier: Modifier = Modifier,
    onClickUrl: (String) -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.clip(CircleShape),
        ) {
            AvatarImage(
                url = comment.creator.avatarUrl,
                modifier = Modifier.size(36.dp),
            )
        }
        Column(
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Text(
                text = comment.creator.nickname ?: "nickname",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = formatDateTime(comment.createdAt),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.slightlyWeaken(),
            )
            SelectionContainer(
                modifier = Modifier.padding(top = 6.dp, end = 24.dp).fillMaxWidth(),
            ) {
                BBCodeView(
                    code = comment.summary,
                    modifier = Modifier.fillMaxWidth(),
                    onClickUrl = onClickUrl,
                )
            }

            if (comment.replies.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        comment.replies.take(3).forEach { reply ->
                            BBCodeView(
                                code = reply.summary,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                brief = true,
                                briefSenderName = reply.creator.nickname ?: "nickname",
                                briefSenderColor = MaterialTheme.colorScheme.primary,
                                onClickUrl = { },
                            )
                        }
                        if (comment.replies.size > 3) {
                            Text(
                                text = "查看更多 ${comment.replies.size - 3} 条回复>",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.5.sp, // todo
                            )
                        }
                    }
                }
            }
        }
    }
}
