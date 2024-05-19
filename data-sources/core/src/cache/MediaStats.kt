package me.him188.ani.datasources.core.cache

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.OptionalFileSize
import me.him188.ani.datasources.api.topic.asOptional
import me.him188.ani.datasources.api.topic.getSpecified
import me.him188.ani.datasources.api.topic.orZero

/**
 * 传输速度信息. 一般继承 [AbstractMediaStats].
 *
 * 为了保证性能, 属性更新可能会有延迟.
 *
 * 注意: 所有属性的值都有可能为 [FileSize.Unspecified], 代表不支持统计该属性. 使用时需要检查.
 */
@Stable
interface MediaStats {
    /**
     * 已上传文件大小. 可能会超过 [size].
     *
     * 注意: 值可能为 [FileSize.Unspecified], 代表不支持统计该属性.
     */
    val uploaded: Flow<OptionalFileSize>

    /**
     * 上传速度, 单位为每秒.
     *
     * 注意: 值可能为 [FileSize.Unspecified], 代表不支持统计该属性.
     */
    val uploadRate: Flow<OptionalFileSize>

    /**
     * 总文件大小
     *
     * 注意: 值可能为 [FileSize.Unspecified], 代表不支持统计该属性.
     */
    val size: Flow<OptionalFileSize>

    /**
     * 已下载文件大小.
     *
     * 注意: 值可能为 [FileSize.Unspecified], 代表不支持统计该属性.
     */
    val downloaded: Flow<OptionalFileSize>

    /**
     * 下载速度, 单位为每秒.
     *
     * 注意: 值可能为 [FileSize.Unspecified], 代表不支持统计该属性.
     */
    val downloadRate: Flow<OptionalFileSize>

    /**
     * 下载进度, 范围为 `[0.0f, 1.0f]`
     *
     * 注意: 值可能为 [Float.NaN], 代表不支持统计该属性.
     */
    val downloadProgress: Flow<Float>

    /**
     * 下载是否完成
     */
    val downloadFinished: Flow<Boolean>
}

@Stable
abstract class AbstractMediaStats : MediaStats {
    // 必须返回同一个实例, 以维持 @Stable 的协定
    override val downloadProgress: Flow<Float> by lazy {
        combine(downloaded, downloaded) { downloaded, total ->
            if (downloaded.isUnspecified || total.isUnspecified) return@combine Float.NaN

            if (total.getSpecified() == FileSize.Zero) Float.NaN
            else downloaded.getSpecified().inBytes.toFloat() / total.getSpecified().inBytes
        }
    }

    override val downloadFinished: Flow<Boolean> by lazy {
        downloadProgress.map { it == 1f }
    }
}

/**
 * 各进度都为 `0`
 */
fun emptyMediaStats(): MediaStats = EmptyMediaStats

// object 惰性初始化
private data object EmptyMediaStats : AbstractMediaStats() {
    override val uploaded: Flow<OptionalFileSize> = flowOf(OptionalFileSize.Zero)
    override val downloaded: Flow<OptionalFileSize> = flowOf(OptionalFileSize.Zero)
    override val uploadRate: Flow<OptionalFileSize> = flowOf(OptionalFileSize.Zero)
    override val size: Flow<OptionalFileSize> = flowOf(OptionalFileSize.Zero)
    override val downloadRate: Flow<OptionalFileSize> = flowOf(OptionalFileSize.Zero)
}

/**
 * 将多个 [MediaStats] 加起来
 */
fun Iterable<MediaStats>.sum(): MediaStats = object : AbstractMediaStats() {
    override val uploaded: Flow<OptionalFileSize> = combine(map { it.uploaded }) { list -> list.sumSpecified() }
    override val downloaded: Flow<OptionalFileSize> = combine(map { it.downloaded }) { list -> list.sumSpecified() }

    override val uploadRate: Flow<OptionalFileSize> = combine(map { it.uploadRate }) { list -> list.sumSpecified() }
    override val size: Flow<OptionalFileSize> = combine(map { it.size }) { list -> list.sumSpecified() }
    override val downloadRate: Flow<OptionalFileSize> = combine(map { it.downloadRate }) { list -> list.sumSpecified() }
}

private fun Array<OptionalFileSize>.sumSpecified() =
    sumOf { it.orZero().inBytes }.bytes.asOptional()
