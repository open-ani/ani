package me.him188.ani.app.ui.settings.rendering

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.datasources.api.source.MediaSourceInfo

@Composable
fun MediaSourceIcon(
    url: String?,
    modifier: Modifier = Modifier,
) {
    if (url == null) {
        Image(
            rememberVectorPainter(Icons.Rounded.DisplaySettings), null,
            modifier,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        )
    } else {
        AsyncImage(
            url,
            null,
            modifier,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            colorFilter = null,
        )

    }
}

/**
 * 宽度不固定
 */
@Composable
fun SmallMediaSourceIcon(
    info: MediaSourceInfo,
    modifier: Modifier = Modifier,
) {
    Box(modifier.clip(MaterialTheme.shapes.extraSmall).height(24.dp)) {
        if (info.imageUrl == null) {
            Image(
                rememberVectorPainter(Icons.Rounded.DisplaySettings),
                null, Modifier.size(24.dp),
            )
        } else {
            AsyncImage(
                info.imageUrl,
                null, Modifier.size(24.dp),
            )
        }
    }
}
