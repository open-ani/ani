package me.him188.ani.app.ui.subject.episode.comments.bbcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.richtext.RichText
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun BBCodeView(
    code: String,
    modifier: Modifier = Modifier,
) {
    val vm = rememberViewModel(keys = listOf(code)) { BBCodeViewViewModel() }
    val elements by vm.elements.collectAsStateWithLifecycle()

    LaunchedEffect(code) {
        vm.parseBBCode(code)
    }

    RichText(
        elements = elements,
        modifier = modifier,
    )
}