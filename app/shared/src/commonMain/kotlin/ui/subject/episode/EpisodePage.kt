package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.preview.PreviewData
import me.him188.ani.app.ui.foundation.AniTopAppBar
import kotlin.time.Duration.Companion.seconds

@Composable
fun EpisodePage(
    viewModel: EpisodeViewModel,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var topAppBarVisible by remember { mutableStateOf(false) }
    val topAppBarAlpha by animateFloatAsState(if (topAppBarVisible) 1f else 0f)
    LaunchedEffect(true) {
        // 启动 2 秒后隐藏 TopAppBar
        launch {
            delay(2.seconds)
            topAppBarVisible = false
        }
    }
    Scaffold(
        topBar = {
            AniTopAppBar(goBack, Modifier.statusBarsPadding().alpha(topAppBarAlpha))
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        EpisodePageContent(
            viewModel,
            onClickVideo = {
                topAppBarVisible = true
            },
            modifier
        )
    }
}

@Composable
fun EpisodePageContent(
    viewModel: EpisodeViewModel,
    onClickVideo: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        EpisodeVideo(onClickVideo)
    }
}

@Composable
private fun EpisodeVideo(
    onClickVideo: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(maxWidth * 9 / 16)
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClickVideo)
        ) { // 16:9 box
            Box(Modifier.matchParentSize().background(Color.Black)) // TODO: video 
        }
    }
}

@Composable
internal fun PreviewEpisodePage() {
    EpisodePageContent(EpisodeViewModel(PreviewData.SOSOU_NO_FURILEN_SUBJECT_ID, 0))
}