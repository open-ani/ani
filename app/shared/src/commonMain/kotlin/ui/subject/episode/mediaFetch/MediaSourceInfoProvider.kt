package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.utils.platform.annotations.TestOnly

/**
 * 用于支持获取 mediaSourceId 的 [MediaSourceInfo]
 */
@Stable
class MediaSourceInfoProvider(
    val getSourceInfoFlow: (mediaSourceId: String) -> Flow<MediaSourceInfo?>,
) {
    @Composable
    fun rememberMediaSourceInfo(mediaSourceId: String): State<MediaSourceInfo?> {
        return remember {
            getSourceInfoFlow(mediaSourceId)
        }.collectAsStateWithLifecycle(null)
    }
}


@Composable
@TestOnly
fun rememberTestMediaSourceInfoProvider(): MediaSourceInfoProvider {
    return remember {
        createTestMediaSourceInfoProvider()
    }
}


@TestOnly
fun createTestMediaSourceInfoProvider(): MediaSourceInfoProvider {
    return MediaSourceInfoProvider(
        getSourceInfoFlow = {
            flowOf(MediaSourceInfo(displayName = it))
        },
    )
}