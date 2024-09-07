package me.him188.ani.app.ui.subject.episode.video

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.repository.EpisodeHistoryRepository
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import kotlin.coroutines.CoroutineContext

@Stable
class EpisodeHistoryState(
    val repository: EpisodeHistoryRepository,
    val playerState: PlayerState,
    currentEpisodeId: Flow<Int>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {

    private val currentEpisodeId by currentEpisodeId.produceState(-1)

    /**
     * 保存当前播放进度
     * 会在 退出播放页前，切换数据源前，切换下一集前 调用
     */
    fun saveProgress() {
        val position = playerState.getExactCurrentPositionMillis()
        launchInBackground {
            repository.saveOrUpdate(currentEpisodeId, position)
        }
    }

    /**
     * 加载当前 ep 播放进度
     */
    fun loadProgress() {
        launchInBackground {
            val positionMillis = repository.getPositionMillisByEpisodeId(episodeId = currentEpisodeId)
            positionMillis?.let {
                playerState.seekTo(positionMillis)
            }
        }
    }

    /**
     * 清除当前 ep 播放进度
     */
    fun clearProgress() {
        launchInBackground {
            repository.remove(episodeId = currentEpisodeId)
        }
    }
}