package me.him188.ani.app.data.source.media.cache

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.sum
import me.him188.ani.utils.coroutines.sampleWithInitial
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 传输速度信息.
 *
 * 实现时一般继承可 [AbstractMediaStats].
 *
 * @see emptyMediaStats
 */
@Stable
interface MediaStats {
    // 实现约束, flow 一定要有至少一个元素.

    /**
     * 总上传量.
     *
     * 实现约束: flow 一定要有至少一个元素.
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val uploaded: Flow<FileSize>

    /**
     * 总下载量.
     *
     * 实现约束: flow 一定要有至少一个元素.
     */
    val downloaded: Flow<FileSize>

    /**
     * 上传速度每秒. 对于不支持上传的缓存, 该值为 [FileSize.Zero].
     *
     * 实现约束: flow 一定要有至少一个元素.
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val uploadRate: Flow<FileSize>

    /**
     * 下载速度, 每秒. 对于不支持下载的缓存, 该值为 [FileSize.Zero]. flow 一定 emit 至少一个元素.
     *
     * - 若 emit [FileSize.Unspecified], 表示上传速度未知. 这只会在该缓存正在上传, 但无法知道具体速度时出现.
     * - 若 emit [FileSize.Zero], 表示上传速度真的是零.
     */
    val downloadRate: Flow<FileSize>
}

@Stable
abstract class AbstractMediaStats : MediaStats

/**
 * 各进度都为 `0` (单元素 flow).
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
 * 将多个 [MediaStats] 加起来 ([combine])
 */
fun Iterable<MediaStats>.sum(): MediaStats = object : AbstractMediaStats() {
    override val uploaded: Flow<FileSize> = combine(map { it.uploaded }) { list -> list.sum() }
    override val downloaded: Flow<FileSize> = combine(map { it.downloaded }) { list -> list.sum() }
    override val uploadRate: Flow<FileSize> = combine(map { it.uploadRate }) { list -> list.sum() }
    override val downloadRate: Flow<FileSize> = combine(map { it.downloadRate }) { list -> list.sum() }
}

fun MediaStats.sampled(period: Duration = 1.seconds): MediaStats {
    class Data(
        val uploaded: FileSize,
        val downloaded: FileSize,
        val uploadRate: FileSize,
        val downloadRate: FileSize,
    )

    return object : MediaStats {
        private val dataFlow = combine(
            this@sampled.uploaded,
            this@sampled.downloaded,
            this@sampled.uploadRate,
            this@sampled.downloadRate,
        ) { uploaded, downloaded, uploadRate, downloadRate ->
            Data(
                uploaded = uploaded,
                downloaded = downloaded,
                uploadRate = uploadRate,
                downloadRate = downloadRate,
            )
        }.sampleWithInitial(period)

        override val uploaded: Flow<FileSize> = dataFlow.map { it.uploaded }
        override val downloaded: Flow<FileSize> = dataFlow.map { it.downloaded }
        override val uploadRate: Flow<FileSize> = dataFlow.map { it.uploadRate }
        override val downloadRate: Flow<FileSize> = dataFlow.map { it.downloadRate }
        override fun toString(): String {
            return "SampledMediaStats(period=$period, original=${this@sampled})"
        }
    }
}
