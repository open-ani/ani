package me.him188.ani.datasources.core.cache

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.sum

/**
 * 传输速度信息. 一般继承 [AbstractMediaStats].
 */
@Stable
interface MediaStats {
    /**
     * Total amount of bytes uploaded
     */
    val uploaded: Flow<FileSize>
    val downloaded: Flow<FileSize>

    val uploadRate: Flow<FileSize>
    val downloadRate: Flow<FileSize>

    val progress: Flow<Float>
}

@Stable
abstract class AbstractMediaStats : MediaStats {
    // 必须返回同一个实例, 以维持 @Stable 的协定
    override val progress: Flow<Float> by lazy {
        combine(uploaded, downloaded) { uploaded, downloaded ->
            if (downloaded == FileSize.Zero) 0f
            else uploaded.inBytes.toFloat() / downloaded.inBytes
        }
    }
}

/**
 * 各进度都为 `0`
 */
fun emptyMediaStats(): MediaStats = EmptyMediaStats

// object 惰性初始化
private data object EmptyMediaStats : AbstractMediaStats() {
    override val uploaded: Flow<FileSize> = flowOf(FileSize.Zero)
    override val downloaded: Flow<FileSize> = flowOf(FileSize.Zero)
    override val uploadRate: Flow<FileSize> = flowOf(FileSize.Zero)
    override val downloadRate: Flow<FileSize> = flowOf(FileSize.Zero)
}

/**
 * 将多个 [MediaStats] 加起来
 */
fun Iterable<MediaStats>.sum(): MediaStats = object : AbstractMediaStats() {
    override val uploaded: Flow<FileSize> = combine(map { it.uploaded }) { list -> list.sum() }
    override val downloaded: Flow<FileSize> = combine(map { it.downloaded }) { list -> list.sum() }
    override val uploadRate: Flow<FileSize> = combine(map { it.uploadRate }) { list -> list.sum() }
    override val downloadRate: Flow<FileSize> = combine(map { it.downloadRate }) { list -> list.sum() }
}