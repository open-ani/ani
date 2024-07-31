package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.source.BangumiCommentSticker
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.richtext.RichTextDefaults
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import org.jetbrains.compose.resources.painterResource

/**
 * A state which is read by Comment composable
 */
@Stable
class CommentState(
    sourceVersion: State<Any?>,
    list: State<List<UIComment>>,
    hasMore: State<Boolean>,
    private val onReload: suspend () -> Unit,
    private val onLoadMore: suspend () -> Unit,
    backgroundScope: CoroutineScope,
) {
    val sourceVersion: Any? by sourceVersion
    val list: List<UIComment> by list

    /**
     * 至少 [onReload] 了一次
     */
    private var loadedOnce by mutableStateOf(false)
    private var freshLoaded by mutableStateOf(false)
    val hasMore: Boolean by derivedStateOf {
        if (!freshLoaded) return@derivedStateOf false
        hasMore.value
    }

    val count by derivedStateOf {
        if (!loadedOnce) null else this.list.size
    }

    private val reloadTasker = MonoTasker(backgroundScope)
    val isLoading get() = reloadTasker.isRunning

    /**
     * 在 LaunchedEffect 中 reload，composition 退出就没必要继续加载
     */
    suspend fun reload() {
        freshLoaded = false
        onReload()
        freshLoaded = true
        loadedOnce = true
    }

    fun loadMore() {
        reloadTasker.launch {
            onLoadMore()
        }
    }
}


@Immutable
class UIRichText(val elements: List<UIRichElement>)

@Immutable
class UIComment(
    val id: Int,
    val creator: UserInfo,
    val content: UIRichText,
    val createdAt: Long, // timestamp millis
    val reactions: List<UICommentReaction>,
    val briefReplies: List<UIComment>,
    val replyCount: Int,
)

@Immutable
class UICommentReaction(
    val id: Int,
    val count: Int,
    val selected: Boolean
)

/**
 * 评论项目
 *
 * @param avatar 用户头像
 * @param primaryTitle 主标题，一般是评论者用户名
 * @param secondaryTitle 副标题，一般是评论发送时间
 * @param rhsTitle 靠右的标题，一般是番剧打分
 * @param content 评论内容
 * @param reactionRow 评论回应的各种表情
 * @param actionRow 评论操作，例如包含回复，添加回应，绝交，举报等按钮
 * @param reply 评论回复
 */
@Composable
fun Comment(
    avatar: @Composable BoxScope.() -> Unit,
    primaryTitle: @Composable ColumnScope.() -> Unit,
    secondaryTitle: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    rhsTitle: @Composable RowScope.() -> Unit = { },
    reactionRow: (@Composable ColumnScope.() -> Unit)? = null,
    actionRow: (@Composable ColumnScope.() -> Unit)? = null,
    reply: (@Composable () -> Unit)? = null,

    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.padding(top = 2.dp).clip(CircleShape)) {
            avatar()
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                        primaryTitle()
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelMedium.copy(
                            color = LocalContentColor.current.slightlyWeaken(),
                        ),
                    ) {
                        secondaryTitle()
                    }
                }
                rhsTitle()
            }
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer(
                modifier = Modifier.fillMaxWidth(),
            ) {
                content()
            }
            if (reactionRow != null) {
                Spacer(modifier = Modifier.height(8.dp))
                SelectionContainer(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    reactionRow()
                }
            }
            if (actionRow != null) {
                SelectionContainer(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    actionRow()
                }
            }
            if (reply != null) {
                Surface(
                    modifier = Modifier.padding(top = if (actionRow == null) 12.dp else 0.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small,
                ) {
                    reply()
                }
            }
        }
    }
}

object CommentDefaults {
    @Composable
    fun Avatar(url: String?) {
        AvatarImage(
            url = url,
            modifier = Modifier.size(36.dp),
        )
    }

    @Composable
    fun Reaction(
        reaction: UICommentReaction,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val backgroundColor by animateColorAsState(
            if (reaction.selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            },
        )
        Surface(
            onClick = onClick,
            modifier = Modifier
                .height(40.dp)
                .padding(vertical = 6.dp)
                .then(modifier),
            enabled = true,
            shape = CircleShape,
            color = backgroundColor,
            border = SuggestionChipDefaults.suggestionChipBorder(true),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val previewing = LocalIsPreviewing.current
                val reactionDrawableRes = BangumiCommentSticker[reaction.id]

                if (previewing || reactionDrawableRes == null) Icon(
                    imageVector = Icons.Rounded.Face,
                    modifier = Modifier.padding(end = 4.dp).size(24.dp),
                    contentDescription = null,
                ) else Icon(
                    painter = painterResource(reactionDrawableRes),
                    modifier = Modifier.padding(end = 4.dp).size(24.dp),
                    contentDescription = null,
                )

                Text(
                    text = reaction.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
    }

    @Composable
    fun ReactionRow(
        list: List<UICommentReaction>,
        modifier: Modifier = Modifier,
        onClickItem: (reactionId: Int) -> Unit,
    ) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            list.forEach {
                Reaction(it) { onClickItem(it.id) }
            }
        }
    }

    @Composable
    fun ActionRow(
        onClickReply: () -> Unit,
        onClickReaction: () -> Unit,
        onClickBlock: () -> Unit,
        onClickReport: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(modifier = modifier) { 
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface.stronglyWeaken(),
            ) {
                IconButton(onClickReply, Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "回复评论",
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClickReaction, Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.AddReaction,
                        contentDescription = "添加表情",
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClickBlock, Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.HeartBroken,
                        contentDescription = "拉黑用户",
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClickReport, Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Report,
                        contentDescription = "举报内容",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    @Composable
    fun ReplyList(
        replies: List<UIComment>,
        modifier: Modifier = Modifier,
        hiddenReplyCount: Int = 0,
        onClickUrl: (String) -> Unit,
        onClickExpand: () -> Unit
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary

        Column(modifier = modifier) {
            replies.forEach { reply ->
                val prepended = remember(reply.content, primaryColor) {
                    reply.content.prependText(
                        prependix = "${reply.creator.nickname ?: reply.creator.id.toString()}：",
                        color = primaryColor,
                    )
                }
                RichText(
                    elements = prepended.elements,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    onClickUrl = onClickUrl,
                )
            }
            if (hiddenReplyCount > 0) {
                val prepended = remember(hiddenReplyCount) {
                    UIRichText(emptyList()).prependText(
                        prependix = "查看更多 $hiddenReplyCount 条回复>",
                        color = primaryColor,
                    )
                }

                RichText(
                    elements = prepended.elements,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable(onClick = onClickExpand),
                    onClickUrl = { },
                )
            }
        }
    }

    // prepend text
    private fun UIRichText.prependText(prependix: String, color: Color): UIRichText = run {
        // 如果 elements 是空的则直接返回一个 annotated text
        val first = elements.firstOrNull()
            ?: return@run listOf(
                UIRichElement.AnnotatedText(
                    listOf(UIRichElement.Annotated.Text(prependix, RichTextDefaults.FontSize, color)),
                ),
            )

        // 如果第一个 element 是 annotated text，则把 prepend 添加到其中
        if (first is UIRichElement.AnnotatedText) {
            listOf(
                first.copy(
                    slice = listOf(
                        UIRichElement.Annotated.Text(prependix, RichTextDefaults.FontSize, color),
                        *first.slice.toTypedArray(),
                    ),
                ),
                *elements.drop(1).toTypedArray(),
            )
        } else { // 如果不是就添加一个 annotated text
            listOf(
                UIRichElement.AnnotatedText(
                    listOf(UIRichElement.Annotated.Text(prependix, RichTextDefaults.FontSize, color)),
                ),
                *elements.toTypedArray(),
            )
        }
    }.let {
        UIRichText(it)
    }
}