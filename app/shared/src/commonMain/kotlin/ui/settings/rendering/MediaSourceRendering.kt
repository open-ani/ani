package me.him188.ani.app.ui.settings.rendering

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.Res
import me.him188.ani.app.acg_rip
import me.him188.ani.app.bangumi
import me.him188.ani.app.data.source.media.cache.MediaCacheManager.Companion.LOCAL_FS_MEDIA_SOURCE_ID
import me.him188.ani.app.dmhy
import me.him188.ani.app.gugufan
import me.him188.ani.app.mikan
import me.him188.ani.app.mxdongman
import me.him188.ani.app.ntdm
import me.him188.ani.app.nyafun
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.xfdm
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import org.jetbrains.compose.resources.painterResource


/*
 * !! 提示: 如果你的 Res 没有找到, 在项目根目录执行以下命令即可: 
 *     ./gradlew build
 */

@Stable
fun renderMediaSource(
    id: String
): String = when (id) {
    "dmhy" -> "動漫花園"
    "acg.rip" -> "ACG.RIP"
    "mikan" -> "Mikan"
    "mikan-mikanime-tv" -> "Mikan (中国大陆)"
    BangumiSubjectProvider.ID -> "Bangumi"
    "nyafun" -> "Nyafun"
    "mxdongman" -> "MX 动漫"
    "ntdm" -> "NT动漫"
    "jellyfin" -> "Jellyfin"
    "emby" -> "Emby"
    "gugufan" -> "咕咕番"
    "xfdm" -> "稀饭动漫"
    LOCAL_FS_MEDIA_SOURCE_ID -> "本地"
    else -> id
}

@Stable
fun renderMediaSourceDescription(
    id: String
): String? = when (id) {
    "dmhy" -> "dmhy.org"
    "acg.rip" -> "acg.rip"
    "mikan" -> "mikanani.me"
    "mikan-mikanime-tv" -> "mikanime.tv"
    BangumiSubjectProvider.ID -> "bgm.tv"
    "nyafun" -> "nyafun.net"
    "mxdongman" -> "mxdm4.com"
    "ikaros" -> "ikaros.run"
    "ntdm" -> "ntdm.tv"
    "gugufan" -> "gugufan.com"
    "xfdm" -> "xfdm.pro"
    LOCAL_FS_MEDIA_SOURCE_ID -> null
    else -> null
}

@Composable
fun getMediaSourceIconResource(
    id: String?
): Painter? {
    if (LocalIsPreviewing.current) { // compose resources does not support preview
        return null
    }
    return when (id) {
        "dmhy" -> painterResource(Res.drawable.dmhy)
        "acg.rip" -> painterResource(Res.drawable.acg_rip)
        "mikan", "mikan-mikanime-tv" -> painterResource(Res.drawable.mikan)
        BangumiSubjectProvider.ID -> painterResource(Res.drawable.bangumi)
        "nyafun" -> painterResource(Res.drawable.nyafun)
        "mxdongman" -> painterResource(Res.drawable.mxdongman)
        "ntdm" -> painterResource(Res.drawable.ntdm)
        "jellyfin" -> rememberVectorPainter(Icons.Rounded.Jellyfin)
        "emby" -> rememberVectorPainter(Icons.Rounded.Emby)
        "gugufan" -> painterResource(Res.drawable.gugufan)
        "xfdm" -> painterResource(Res.drawable.xfdm)
        else -> null
    }
}

@Composable
fun MediaSourceIcon(
    id: String,
    modifier: Modifier = Modifier,
    url: String? = null,
) {
    if (url != null) {
        AsyncImage(
            url,
            null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
        )
    } else {
        val ic = getMediaSourceIconResource(id)
        Image(
            ic
                ?: rememberVectorPainter(Icons.Rounded.DisplaySettings),
            null,
            modifier,
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            colorFilter = if (ic == null) ColorFilter.tint(MaterialTheme.colorScheme.onSurface) else null,
        )
    }
}

/**
 * 宽度不固定
 */
@Composable
fun SmallMediaSourceIcon(
    id: String?,
    modifier: Modifier = Modifier,
    allowText: Boolean = true,
) {
    Box(modifier.clip(MaterialTheme.shapes.extraSmall).height(24.dp)) {
        val icon = getMediaSourceIconResource(id)
        if (icon == null) {
            if (allowText && id != null) {
                Text(
                    renderMediaSource(id),
                    Modifier.height(24.dp),
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            } else {
                Icon(Icons.Rounded.DisplaySettings, id)
            }
        } else {
            Image(icon, null, Modifier.size(24.dp))
        }
    }
}
