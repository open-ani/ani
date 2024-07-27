package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.richtext.RichText
import me.him188.ani.app.ui.foundation.richtext.RichTextDefaults
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken

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
        Box(modifier = Modifier.clip(CircleShape)) {
            avatar()
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                        primaryTitle()
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
            Spacer(modifier = Modifier.height(6.dp))
            SelectionContainer(
                modifier = Modifier.padding(end = 24.dp).fillMaxWidth(),
            ) {
                content()
            }
            if (reactionRow != null) {
                Spacer(modifier = Modifier.height(6.dp))
                SelectionContainer(
                    modifier = Modifier.padding(end = 24.dp).fillMaxWidth(),
                ) {
                    reactionRow()
                }
            }
            if (actionRow != null) {
                Spacer(modifier = Modifier.height(6.dp))
                SelectionContainer(
                    modifier = Modifier.padding(end = 24.dp).fillMaxWidth(),
                ) {
                    actionRow()
                }
            }
            if (reply != null) {
                Surface(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
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
    fun ReplyList(
        replies: List<UIComment>,
        replyCount: Int = replies.size,
        onClickUrl: (String) -> Unit = { },

        ) {
        val primaryColor = MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            replies.forEach { reply ->
                val prepended by remember {
                    derivedStateOf {
                        reply.content.prependText(
                            prependix = "${reply.creator.nickname ?: reply.creator.id.toString()}：",
                            color = primaryColor,
                        )
                    }
                }
                RichText(
                    elements = prepended.elements,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    onClickUrl = onClickUrl,
                )
            }
            if (replyCount > 3) {
                val prepended by remember {
                    derivedStateOf {
                        UIRichText(emptyList()).prependText(
                            prependix = "查看更多 ${replyCount - 3} 条回复>",
                            color = primaryColor,
                        )
                    }
                }

                RichText(
                    elements = prepended.elements,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
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