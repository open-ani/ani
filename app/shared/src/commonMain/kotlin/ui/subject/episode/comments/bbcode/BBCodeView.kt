package me.him188.ani.app.ui.subject.episode.comments.bbcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.richtext.RichText
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

/**
 * @param brief 解析为简要文本，简要文本会将图片转换为 `[图片]`，忽略引用，并且不包含任何格式
 */
@Composable
fun BBCodeView(
    code: String,
    modifier: Modifier = Modifier,
    brief: Boolean = false,
    briefSenderName: String = "",
    briefSenderColor: Color = Color.Unspecified,
    onClickUrl: (String) -> Unit, 
) {
    val vm = rememberViewModel(keys = listOf(code, brief)) { BBCodeViewViewModel() }
    val elements by vm.elements.collectAsStateWithLifecycle()

    LaunchedEffect(code, brief) {
        if (!brief) {
            vm.parse(code)
        } else {
            vm.parseAsReply(code, briefSenderName, briefSenderColor)
        }
    }

    RichText(
        elements = elements,
        modifier = modifier,
        onClickUrl = onClickUrl,
    )
}