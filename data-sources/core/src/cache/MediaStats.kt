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
}

@Stable
abstract class AbstractMediaStats : MediaStats

/**
 * 各进度都为 `0`
 */
fun emptyMediaStats(): MediaStats = EmptyMediaStats

// object 惰性初始化
private data object EmptyMediaStats : AbstractMediaStats() {
    private val ZERO_FLOW = flowOf(FileSize.Zero)
    override val uploaded: Flow<FileSize> get() = ZERO_FLOW
    override val downloaded: Flow<FileSize> get() = ZERO_FLOW
    override val uploadRate: Flow<FileSize> get() = ZERO_FLOW
    override val downloadRate: Flow<FileSize> get() = ZERO_FLOW
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