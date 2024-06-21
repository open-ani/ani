package me.him188.ani.app.ui.foundation.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Stable
inline val ProgressIndicatorHeight get() = 4.dp

@Composable
fun AnimatedLinearProgressIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
) {
    androidx.compose.animation.AnimatedVisibility(
        visible,
        modifier
            .padding(horizontal = 4.dp) // m3 spec
            .height(ProgressIndicatorHeight),
        enter = expandVertically(tween(1000), expandFrom = Alignment.CenterVertically),
        exit = shrinkVertically(tween(1000), shrinkTowards = Alignment.CenterVertically),
    ) {
        Crossfade(progress == null) {
            if (progress == null) {
                LinearProgressIndicator(
                    Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    strokeCap = StrokeCap.Round,
                )
            } else {
                LinearProgressIndicator(
                    progress,
                    Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}
