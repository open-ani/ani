package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.navigation.LocalNavigator

@Composable
fun EpisodeScene(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        val navigator = LocalNavigator.current
        EpisodePage(
            viewModel,
            goBack = { navigator.navigator.goBack() },
            modifier = modifier,
        )
    }
}