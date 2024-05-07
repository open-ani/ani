package me.him188.ani.app.tools.caching

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.utils.coroutines.cancellableCoroutineScope

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
)
annotation class UnsafeLazyDataCacheApi

/**
 * A data collection, where the data is loaded from a remote source.
 *
 * The data is loaded lazily, i.e. only if [requestMore] is called.
 *
 * See the constructor-like factory function for more details.
 */
@Stable
interface LazyDataCache<T> {
    /**
     * 当前缓存的数据, 它可能是不完整的.
     *
     * @see data
     * @see ContentPolicy
     */
    val cachedData: StateFlow<List<T>>

    /**
     * 完整的数据 flow. 在 collect 这个 flow 时将会自动调用 [requestMore] 加载更多数据.
     * 这个 flow 会在每加载完成一页时返回累计的数据.
     *
     * 该 flow 会在加载完成所有数据后完结, 且一定会至少 emit 一次.
     *
     * 如要获取所有数据, 可使用 `allData.last()`.
     *
     * 若在 [allData] collect 的过程中有 [invalidate], 那么 [allData] 将会重新开始.
     *
     * @see data
     * @see ContentPolicy
     */
    val allData: Flow<List<T>>

    /**
     * Whether the current remote flow has been exhausted.
     *
     * Note that when [isCompleted] is seen as `true`, it can still become `false` later if the remote source has restarted (in which case [cachedData] will be cleared).
     */
    val isCompleted: Flow<Boolean>

    /**
     * Total size of the data. It can change if [requestMore].
     *
     * It can be `null` if not known.
     */
    val totalSize: Flow<Int?>

    /**
     * Lock for cross-data-cache operations. Only for internal use.
     */
    @UnsafeLazyDataCacheApi
    val lock: Mutex

    @UnsafeLazyDataCacheApi
    val mutator: LazyDataCacheMutator<T>

    /**
     * Attempts to load more data.
     * Returns `false` if the remote flow has already been exhausted, i.e. all data has successfully loaded.
     * Returns `true` when the next page is loaded.
     *
     * The will only be one data loading operation at a time.
     *
     * This function performs calculations on the [Dispatchers.Default] dispatcher.
     *
     * 此函数可以在 UI 或者其他线程调用.
     */
    suspend fun requestMore(): Boolean

    /**
     * 重新从头开始加载数据, 当加载成功时替换掉当前缓存. 当加载失败时不修改当前缓存.
     *
     * 注意, [refresh] 与先 [invalidate] 再 [requestMore] 不同:
     * [refresh] 只会在加载成功后替换当前缓存, 而 [invalidate] 总是会清空当前缓存.
     *
     * This function supports coroutine cancellation.
     *
     * 此函数可以在 UI 或者其他线程调用.
     */
    suspend fun refresh()

    /**
     * 清空所有本地缓存以及当前的 [PagedSource]. 注意, 本函数不会请求数据. 即当函数返回时, [cachedData] 为空.
     */
    suspend fun invalidate()
}

/**
 * Changes the [LazyDataCache.cachedData] under a lock.
 *
 * Note, you must not call [mutate] again within [action], as it will cause a deadlock.
 * @see LazyDataCacheMutator
 */
@OptIn(UnsafeLazyDataCacheApi::class)
suspend inline fun <R, T> LazyDataCache<T>.mutate(action: LazyDataCacheMutator<T>.() -> R): R {
    return lock.withLock {
        action(mutator)
    }
}

/**
 * Mutates two or more [LazyDataCache]s atomically.
 */
@OptIn(UnsafeLazyDataCacheApi::class)
suspend inline fun <T> dataTransaction(
    vararg caches: LazyDataCache<T>,
    crossinline action: suspend (data: List<LazyDataCacheMutator<T>>) -> Unit,
) {
    val lockedLocks = mutableListOf<Mutex>()
    try {
        caches.sortedBy { it.hashCode() } // prevent deadlocks
            .forEach {
                it.lock.lock(lockedLocks)
                lockedLocks.add(it.lock)
            }

        action(caches.map { it.mutator })
    } finally {
        for (lockedLock in lockedLocks) {
            lockedLock.unlock(lockedLocks)
        }
    }
}

inline val <T> LazyDataCache<T>.value get() = cachedData.value

/**
 * Creates a [LazyDataCache].
 *
 * [createSource] will be called on demand to create a flow of pages.
 *
 * On [LazyDataCache.requestMore], the [PagedSource.nextPageOrNull] will be called to load more data.
 * If [PagedSource.nextPageOrNull] returns `null`, it is considered to be end of the data.
 *
 * `nextPageOrNull` must not throw any exceptions, if it does, it is considered to be end of the data.
 *
 * Completion of [LazyDataCache] relies on the completion of the [createSource] flow.
 */
@OverloadResolutionByLambdaReturnType
fun <T> LazyDataCache(
    createSource: suspend LazyDataCacheContext.() -> PagedSource<T>,
    sanitize: (suspend (List<T>) -> List<T>)? = null,
    debugName: String? = null,
): LazyDataCache<T> = LazyDataCacheImpl(createSource, sanitize, debugName)

interface LazyDataCacheContext {
    /**
     * 清空所有本地缓存以及当前的 [PagedSource].
     */
    suspend fun invalidate()
}

class LazyDataCacheImpl<T>(
    private val createSource: suspend LazyDataCacheContext.() -> PagedSource<T>,
    private val sanitize: (suspend (List<T>) -> List<T>)? = null,
    private val debugName: String? = null,
) : LazyDataCache<T>, LazyDataCacheContext {
    override val cachedData: MutableStateFlow<List<T>> = MutableStateFlow(emptyList())

    // Writes must be under lock
    private suspend inline fun setData(data: List<T>) {
        cachedData.value = sanitize?.invoke(data) ?: data
    }

    override val allData: Flow<List<T>> = flow {
        cancellableCoroutineScope {
            currentSource.collectLatest { // 当有新 source 时 flow 会重新开始
                emit(cachedData.value)
                while (!sourceCompleted.first()) {
                    requestMore()
                    emit(cachedData.value)
                }
                cancelScope() // 仅收集一个 source
            }
        }
    }

    private val requestInProgress = MutableStateFlow(false)

    @OptIn(UnsafeLazyDataCacheApi::class)
    override val lock: Mutex = Mutex()

    @OptIn(UnsafeLazyDataCacheApi::class)
    override val mutator: LazyDataCacheMutator<T> = object : LazyDataCacheMutator<T>() {
        override suspend fun update(map: (List<T>) -> List<T>) {
            setData(map(cachedData.value))
        }
    }

    // Writes must be under lock
    private val currentSource: MutableStateFlow<PagedSource<T>?> = MutableStateFlow(null)
    private val sourceCompleted = currentSource.flatMapLatest { it?.finished ?: flowOf() }

    override val totalSize: Flow<Int?> = currentSource.transformLatest { source ->
        if (source == null) {
            emit(null)
            return@transformLatest
        }
        emitAll(source.totalSize)
    }

    override suspend fun requestMore(): Boolean {
        // impl notes:
        // 这函数必须支持 cancellation, 因为它会在 composition 线程调用

        return withContext(Dispatchers.IO) {
            lock.withLock {
                val source = getSourceOrCreate()

                try {
                    requestInProgress.value = true
                    val resp = source.nextPageOrNull() // cancellation-supported
                    return@withContext if (resp != null) {
                        try {
                            setData(cachedData.value + resp)
                        } catch (e: CancellationException) {
                            // Data is not updated, we should not progress the source page
                            source.backToPrevious()
                            throw e
                        }
                        true
                    } else {
                        false
                    }
                } finally {
                    requestInProgress.value = false
                }
            }
        }
    }

    // Unsafe, must be used under lock.
    private suspend fun LazyDataCacheImpl<T>.getSourceOrCreate(): PagedSource<T> {
        currentSource.value?.let { return it }
        return createSource().also {
            currentSource.value = it
        }
    }

    override suspend fun refresh() {
        lock.withLock {
            withContext(Dispatchers.IO) {
                val source = createSource() // note: always create a new source
                try {
                    requestInProgress.value = true
                    val resp = source.nextPageOrNull() // cancellation-supported

                    // Update source only if the request was successful, as per documentation on [refresh]
                    currentSource.value = source
                    cachedData.value = resp.orEmpty()
                } finally {
                    requestInProgress.value = false
                }
            }
        }
    }

    override val isCompleted =
        combine(requestInProgress, sourceCompleted, currentSource) { requestInProgress, sourceCompleted, source ->
            source?.finished ?: flowOf(sourceCompleted && !requestInProgress)
        }.flatMapLatest { it }

    override suspend fun invalidate() {
        lock.withLock {
            currentSource.value = null
            cachedData.value = emptyList()
        }
    }
}