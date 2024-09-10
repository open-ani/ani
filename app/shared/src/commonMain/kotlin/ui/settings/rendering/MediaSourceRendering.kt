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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import me.him188.ani.app.Res
import me.him188.ani.app.acg_rip
import me.him188.ani.app.dmhy
import me.him188.ani.app.gugufan
import me.him188.ani.app.mikan
import me.him188.ani.app.mxdongman
import me.him188.ani.app.ntdm
import me.him188.ani.app.nyafun
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.xfdm
import me.him188.ani.datasources.api.source.MediaSourceInfo
import org.jetbrains.compose.resources.painterResource

@Composable
fun MediaSourceInfo.getIconResourceOrNull(): Painter? {
    val res = when (this.iconResourceId) {
        "mikan.png" -> Res.drawable.mikan
        "acg-rip.png" -> Res.drawable.acg_rip
        "dmhy.png" -> Res.drawable.dmhy
        "gugufan.png" -> Res.drawable.gugufan
        "mxdongman.png" -> Res.drawable.mxdongman
        "ntdm.png" -> Res.drawable.ntdm
        "nyafun.png" -> Res.drawable.nyafun
        "xfdm.png" -> Res.drawable.xfdm
        else -> null
    }
    return res?.let { painterResource(it) }
}

@Composable
fun MediaSourceIcon(
    sourceInfo: MediaSourceInfo?,
    modifier: Modifier = Modifier,
) {
    val url = sourceInfo?.getIconResourceOrNull()
    when {
        url != null && !LocalIsPreviewing.current -> { // TODO: 升级到 CMP 1.7 后, 可以去掉这里的 LocalIsPreviewing
            Image(
                url,
                null,
                modifier,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                colorFilter = null,
            )
        }

        sourceInfo?.iconUrl != null -> {
            AsyncImage(
                sourceInfo.iconUrl,
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
        val image = info.getIconResourceOrNull()
        when {
            image != null -> {
                Image(
                    image,
                    null, Modifier.size(24.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    colorFilter = null,
                )
            }

            info.iconUrl != null -> {
                AsyncImage(
                    info.iconUrl,
                    null, Modifier.size(24.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    colorFilter = null,
                )
            }

            else -> {
                Image(
                    rememberVectorPainter(Icons.Rounded.DisplaySettings),
                    null, Modifier.size(24.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    colorFilter = null,
                )
            }
        }
    }
}
