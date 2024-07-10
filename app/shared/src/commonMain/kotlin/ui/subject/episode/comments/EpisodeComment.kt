package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.flow.collectLatest
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
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

    Box(modifier = modifier) {
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
            itemsIndexed(comments) { index, item ->
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
    modifier: Modifier = Modifier
) {
    val constraintSet = ConstraintSet {
        val (avatar, nickname, time, indicator, content) =
            createRefsFor("avatar", "nickname", "time", "indicator", "content")

        constrain(avatar) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }
        constrain(nickname) {
            top.linkTo(parent.top)
            start.linkTo(avatar.end, 12.dp)
        }
        constrain(time) {
            top.linkTo(nickname.bottom, 4.dp)
            start.linkTo(avatar.end, 12.dp)
        }
        constrain(indicator) {
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        }
        constrain(content) {
            start.linkTo(avatar.end, 12.dp)
            top.linkTo(time.bottom, 12.dp)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    }
    ConstraintLayout(
        constraintSet = constraintSet,
        modifier = modifier,
    ) {
        Box(
            Modifier.layoutId("avatar").clip(CircleShape),
        ) {
            AvatarImage(
                comment.creator.avatarUrl,
                Modifier.size(36.dp),
            )
        }
        Text(
            comment.id,
            modifier = Modifier.layoutId("indicator"),
            style = MaterialTheme.typography.labelMedium,

            )
        SelectionContainer(modifier = Modifier.layoutId("nickname")) {
            Text(
                comment.creator.nickname ?: "nickname",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        SelectionContainer(modifier = Modifier.layoutId("time")) {
            Text(
                formatDateTime(comment.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.slightlyWeaken(),
            )
        }
        SelectionContainer(modifier = Modifier.layoutId("content")) {
            Text(
                comment.summary,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 15.5.sp,
            )
        }
    }
}
