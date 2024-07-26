package me.him188.ani.app.ui.foundation.effects

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel


@Composable
actual fun ScreenRotationEffectImpl(vm: EpisodeViewModel) {
    if (!vm.videoScaffoldConfig.autoFullscreenOnLandscapeMode) return
    val configuration = LocalConfiguration.current
    LaunchedEffect(key1 = true) {
        vm.isFullscreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}