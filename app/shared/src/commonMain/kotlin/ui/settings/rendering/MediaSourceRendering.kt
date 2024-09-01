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
import me.him188.ani.app.Res
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.datasources.api.source.MediaSourceInfo

fun MediaSourceInfo.getResourceOrImageUri(): String? {
    return iconResourceId?.let { Res.getUri("drawable/$it") } ?: iconUrl
}

@Composable
fun MediaSourceIcon(
    sourceInfo: MediaSourceInfo?,
    modifier: Modifier = Modifier,
) {
    val url = sourceInfo?.getResourceOrImageUri()
    when {
        url != null && !LocalIsPreviewing.current -> { // TODO: 升级到 CMP 1.7 后, 可以去掉这里的 LocalIsPreviewing
            AsyncImage(
                url,
                null,
                modifier,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                colorFilter = null,
            )
        }

        else -> {
            Image(
                rememberVectorPainter(Icons.Rounded.DisplaySettings), null,
                modifier,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            )
        }
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
        val image = info.getResourceOrImageUri()
        if (image == null) {
            Image(
                rememberVectorPainter(Icons.Rounded.DisplaySettings),
                null, Modifier.size(24.dp),
            )
        } else {
            AsyncImage(
                image,
                null, Modifier.size(24.dp),
            )
        }
    }
}
