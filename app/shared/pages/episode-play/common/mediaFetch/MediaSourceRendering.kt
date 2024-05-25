package me.him188.ani.app.ui.subject.episode.mediaFetch

/*
 * !! 提示: 如果你的 Res 没有找到, 在项目根目录执行以下命令即可: 
 *     ./gradlew generateComposeResClass prepareAppResources
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter
import me.him188.ani.app.Res
import me.him188.ani.app.acg_rip
import me.him188.ani.app.bangumi
import me.him188.ani.app.data.media.MediaCacheManager.Companion.LOCAL_FS_MEDIA_SOURCE_ID
import me.him188.ani.app.dmhy
import me.him188.ani.app.mikan
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import me.him188.ani.datasources.nyafun.NyafunMediaSource
import org.jetbrains.compose.resources.painterResource

@Stable
fun renderMediaSource(
    id: String
): String = when (id) {
    DmhyMediaSource.ID -> "動漫花園"
    AcgRipMediaSource.ID -> "ACG.RIP"
    MikanMediaSource.ID -> "Mikan"
    MikanCNMediaSource.ID -> "Mikan (中国大陆)"
    BangumiSubjectProvider.ID -> "Bangumi"
    NyafunMediaSource.ID -> "Nyafun"
    LOCAL_FS_MEDIA_SOURCE_ID -> "本地"
    else -> id
}

@Stable
fun renderMediaSourceDescription(
    id: String
): String? = when (id) {
    DmhyMediaSource.ID -> "dmhy.org"
    AcgRipMediaSource.ID -> "acg.rip"
    MikanMediaSource.ID -> "mikanani.me"
    MikanCNMediaSource.ID -> "mikanime.tv"
    BangumiSubjectProvider.ID -> "bgm.tv"
    NyafunMediaSource.ID -> "nyafun.net"
    LOCAL_FS_MEDIA_SOURCE_ID -> null
    else -> null
}

@Composable
fun getMediaSourceIcon(
    id: String?
): Painter? {
    if (LocalIsPreviewing.current) { // compose resources does not support preview
        return null
    }
    return when (id) {
        DmhyMediaSource.ID -> painterResource(Res.drawable.dmhy)
        AcgRipMediaSource.ID -> painterResource(Res.drawable.acg_rip)
        MikanMediaSource.ID, MikanCNMediaSource.ID -> painterResource(Res.drawable.mikan)
        BangumiSubjectProvider.ID -> painterResource(Res.drawable.bangumi)
        else -> null
    }
}
