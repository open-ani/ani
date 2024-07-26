package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.Composable
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel

@Composable
fun ScreenRotationEffect(vm: EpisodeViewModel) = ScreenRotationEffectImpl(vm) // workaround for IDE completion bug

@Composable
expect fun ScreenRotationEffectImpl(vm: EpisodeViewModel)
