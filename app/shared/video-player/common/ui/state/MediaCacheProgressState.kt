package me.him188.ani.app.videoplayer.ui.state

import androidx.compose.runtime.Stable

/**
 * 视频播放器进度条的缓存进度
 */
@Stable
interface MediaCacheProgressState {
    /**
     * 区块列表. 每个区块的宽度由 [Chunk.weight] 决定.
     *
     * 所有 chunks 的 weight 之和应当 (约) 等于 1, 否则将会导致绘制超出进度条的区域 (即会被忽略).
     */
    val chunks: List<Chunk>

    /**
     * 当前的版本. 当 [chunks] 更新时, 该值会递增.
     */
    val version: Int

    /**
     * 是否已经全部缓存完成. 当已经缓存完成时, UI 可能会优化性能, 不再考虑 [chunks] 更新.
     */
    val isFinished: Boolean
}

interface UpdatableMediaCacheProgressState : MediaCacheProgressState {
    suspend fun update()
}

// Not stable
interface Chunk {
    @Stable
    val weight: Float // always return the same value

    // This can change, and change will not notify compose state
    val state: ChunkState
}

enum class ChunkState {
    /**
     * 初始状态
     */
    NONE,

    /**
     * 正在下载
     */
    DOWNLOADING,

    /**
     * 下载完成
     */
    DONE,

    /**
     * 对应 BT 的没有任何 peer 有这个 piece 的状态
     */
    NOT_AVAILABLE
}

private object EmptyMediaCacheProgressState : UpdatableMediaCacheProgressState {
    override suspend fun update() {
    }

    override val chunks: List<Chunk> get() = emptyList()
    override val version: Int get() = 0
    override val isFinished: Boolean get() = false
}

@Stable
fun emptyMediaCacheProgressState(): UpdatableMediaCacheProgressState = EmptyMediaCacheProgressState
