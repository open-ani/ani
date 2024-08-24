package me.him188.ani.app.tools.caching

import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastDistinctBy
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.fold
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
)
annotation class UnsafeLazyDataCacheApi

/**
 * 一个能在本地缓存的远程数据集合.
 *
 * 数据将在需要时惰性请求, 缓存到本地并自动持久化.
 *
 * ### 获取数据流
 *
 * - 通过 [cachedDataFlow], 获取缓存的数据流. 该数据流只访问本地的缓存数据, 不会自动发送网络请求. 需要调用 [requestMore] 以请求下一页.
 * - 通过 [allDataFlow], 获取完整的数据流. 该数据流会自动连续地请求下一页数据, 直到远程数据全部加载完成.
 *
 * ### 修改缓存数据
 *
 * 缓存数据可被安全地修改并自动持久化.
 *
 * 可通过 [mutate] 修改缓存数据, 或者使用 [dataTransaction] 同时修改多个 [LazyDataCache].
 *
 * ### 持久化与缓存一致性
 *
 * 所有数据都会持久化到本地, 且只会在成功持久化之后, 才可通过任意方式获取. 这意味着:
 * - 当 [cachedDataFlow] emit 新的值时, 该数据一定已经持久化成功了
 * - 当 [requestMore] 返回 `true` 时, 缓存一定已经更新成功并且持久化成功
 *
 * 支持持久化的, 不只是列表数据本身, 还包括页码和总大小 [totalSize].
 * - 页码持久化后, 下次启动时将会从上次停止的地方继续加载数据.
 *
 * [cachedDataFlow] 与 [totalSize] 拥有相同的可见性. 这意味着:
 * - 当 [cachedDataFlow] emit 新的值时, [totalSize] 一定也会能 emit 新的值
 * - 它们一定是在同一瞬时时间之后获得 emit 新的值的能力. 换句话说,
 *   如果此时 [cachedDataFlow] emit 了新的值, 那么一个并行线程调用 `totalSize.first()` 也能立即看到新值.
 */
@Stable
interface LazyDataCache<T> {
    /**
     * 获取当前已经从网络上加载下来的缓存的数据的 flow.
     *
     * 这是一个不会完结的 flow. [collect][Flow.collect] 这个 flow 只会看到本地缓存数据, 不会触发网络请求.
     * 即使目前页码没有到最后 (即远程服务器还有更多数据), 也不会执行网络请求下一页. 必须调用 [requestMore] 显式请求下一页, [cachedDataFlow] 才会更新.
     *
     * 可使用 [mutate] 修改 [cachedDataFlow].
     *
     * @see data
     * @see ContentPolicy
     */
    val cachedDataFlow: Flow<List<T>>

    val state: Flow<LazyDataCacheState>

    /**
     * 完整的数据 flow. 在 collect 这个 flow 时, 将会首先 emit [cachedDataFlow],
     * 随后不断地自动调用 [requestMore] 加载更多数据并 emit 新的数据 list, 直到远程数据全部加载完成.
     *
     * 这个 flow 会在每加载完成一页时返回累计的 list.
     *
     * 若在 [allDataFlow] collect 的过程中有 [invalidate], 那么 [allDataFlow] 将会立即 emit [emptyList], 然后重新开始加载所有远程数据.
     *
     * @see data
     * @see ContentPolicy
     */
    val allDataFlow: Flow<List<T>>

    /**
     * Whether the current remote flow has been exhausted.
     *
     * Note that when [isCompleted] is seen as `true`, it can still become `false` later if the remote source has restarted (in which case [cachedDataFlow] will be cleared).
     */
    val isCompleted: Flow<Boolean>

    /**
     * 最后更新时间. [requestMore], [invalidate], [refresh] 都会更新这个时间.
     */
    val lastUpdated: Flow<Long>

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

    /**
     * 用于修改缓存内容. 该属性属于内部低级 API, 请使用高级的 [mutate] 和 [dataTransaction].
     *
     * @see mutate
     * @see dataTransaction
     */
    @UnsafeLazyDataCacheApi
    val mutator: LazyDataCacheMutator<T>

    /**
     * 尝试请求下一页数据. 当请求成功时, 会将新的数据追加到当前缓存中.
     * 如果已知已经没有更多页码了, 本函数立即返回 `false`, 不会修改缓存.
     *
     * 当成功请求一页数据并追加和持久化缓存后返回 `true`.
     * 当没有修改缓存时返回 `false`, 可能意味着最后一页早就已经加载完了而已经没有更多的页码了, 也可能意味着请求失败了
     *
     * ## 线程安全
     *
     * 同一时间只会有一个 [requestMore] 进行中. 多次调用 [requestMore] 时, 会等待上一个请求完成后再执行下一个请求.
     * 如果上一个请求已经导致加载完了所有页码, 则后续请求会直接返回.
     *
     * 此函数可以在 UI 或者其他线程调用.
     *
     * ## 异常将被捕获
     *
     * 如果在请求网络时遇到异常, 本函数会捕获异常, 并更新到 [state]
     *
     * ## 支持 Coroutine Cancellation
     *
     * 此函数的内部任意阶段都可被取消. 当协程被取消时, 页码和当前缓存都不会变更.
     * 即使已经成功查询了一页, 页码也会被恢复到调用该函数之前的值.
     */
    suspend fun requestMore(): Boolean

    /**
     * 重新从头开始加载数据, 当加载成功时替换掉当前缓存. 当加载失败时不修改当前缓存.
     *
     * 注意, [refresh] 与先 [invalidate] 再 [requestMore] 不同:
     * [refresh] 只会在加载成功后替换当前缓存, 而 [invalidate] 总是会清空当前缓存.
     *
     * 此函数可以在 UI 或者其他线程调用.
     *
     * ## 异常将被捕获
     *
     * 如果在请求网络时遇到异常, 本函数会捕获异常, 并更新到 [state]
     *
     * ## 支持 Coroutine Cancellation
     *
     * 此函数的内部任意阶段都可被取消. 当协程被取消时, 缓存不会变更.
     *
     * @param orderPolicy 刷新时的顺序. 查看 [RefreshOrderPolicy].
     */
    suspend fun refresh(orderPolicy: RefreshOrderPolicy): Boolean

    /**
     * 清空所有本地缓存以及当前的 [PagedSource]. 注意, 本函数不会请求数据. 即当函数返回时, [cachedDataFlow] 为空.
     */
    suspend fun invalidate()
}

sealed class LazyDataCacheState {
    /**
     * 正常状态. 因为 [LazyDataCache] 是懒加载的, 所以一开始是没有数据的, 这时状态为 [Normal].
     * 当加载列表成功时也是 [Normal].
     */
    data object Normal : LazyDataCacheState()

    data class ApiError(
        val reason: ApiFailure,
    ) : LazyDataCacheState()

    data class UnknownError(
        val throwable: Throwable,
    ) : LazyDataCacheState()
}

enum class RefreshOrderPolicy {
    /**
     * 尽量保持原有的顺序, 新的物品出现在底部.
     *
     * 该模式适合被动刷新.
     * 例如在刚刚启动 app 时自动在后台请求追番列表,
     * 但由于请求列表需要耗费数秒, 用户在这期间可能已经操作 (滑动) 了列表.
     * 当请求完成后, 如果整体替换列表, 可能导致元素位置变换, 影响体验, 所以应当保持已有元素的顺序.
     *
     * [LazyDataCache.requestMore] 总是使用这个策略.
     *
     * 如果在 [LazyDataCache.refresh] 时使用这个策略,
     * 将会使用 [Any.equals] 比较新旧数据, 从当前缓存中去除新列表中不存在元素, 然后将新列表中的新元素追加到末尾.
     */
    KEEP_ORDER_APPEND_LAST,

    /**
     * 不保持原有顺序, 按照新的数据顺序排列.
     *
     * 如果在 [LazyDataCache.refresh] 时使用这个策略, 则会使用新的 list 整个替换掉旧的 list.
     */
    REPLACE,
}

/**
 * 线程安全地修改一个 [LazyDataCache.cachedDataFlow].
 * 修改完成后将会持久化, 随后通过 [LazyDataCache.cachedDataFlow] 传播给其他订阅者.
 *
 * 如果 [action] 抛出异常, 该异常将会被原封不动地抛出, 并且不会修改缓存.
 *
 * 如需同时修改多个 [LazyDataCache], 使用 [dataTransaction]
 *
 * @param action 在锁里执行的操作, 其中不可以重复调用相同 [this] 的 [mutate] 或 [dataTransaction].
 *
 * @sample me.him188.ani.app.tools.caching.LazyDataCacheSamples.mutate
 *
 * @see LazyDataCacheMutator
 */
@OptIn(UnsafeLazyDataCacheApi::class)
suspend inline fun <T> LazyDataCache<T>.mutate(action: LazyDataCacheMutator<T>.() -> Unit) {
    return lock.withLock {
        action(mutator)
    }
}

/**
 * 线程安全地同时修改多个 [LazyDataCache]. 可用于移动数据等操作.
 *
 * ### 示例
 * 将 `from` 中的特定 ID 的 subject 移动到 `target`:
 * ```
 * dataTransaction(from, target) { (f, t) ->
 *     val old = f.removeFirstOrNull { it.subjectId == subjectId } ?: return@dataTransaction
 *     t.addFirst(
 *         old.copy(collectionType = type),
 *     )
 * }
 * ```
 */
@OptIn(UnsafeLazyDataCacheApi::class)
suspend inline fun <T> dataTransaction(
    vararg caches: LazyDataCache<T>,
    crossinline action: suspend (data: List<LazyDataCacheMutator<T>>) -> Unit,
) {
    val locker = Any()
    val lockedLocks = mutableListOf<Mutex>()
    try {
        caches.distinct().sortedBy { it.hashCode() } // prevent deadlocks
            .forEach {
                it.lock.lock(locker)
                lockedLocks.add(it.lock)
            }

        action(caches.map { it.mutator })
    } finally {
        for (lockedLock in lockedLocks) {
            lockedLock.unlock(locker)
        }
    }
}

suspend inline fun <T> LazyDataCache<T>.getCachedData() = cachedDataFlow.first()

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
 *
 * @param persistentStore 用于将内存缓存数据持久化(到文件系统). 默认为 [MemoryDataStore], 即仅在内存中保存.
 * 可通过 [DataStoreFactory] 创建一个使用 File 进行持久化的实例.
 */
fun <T> LazyDataCache(
    createSource: suspend LazyDataCacheContext.() -> ApiResponse<PagedSource<T>>,
    getKey: (T) -> Any? = { it },
    debugName: String? = null,
    persistentStore: DataStore<LazyDataCacheSave<T>> = defaultPersistentStore()
): LazyDataCache<T> = LazyDataCacheImpl(createSource, getKey, debugName, persistentStore)

private fun <T> defaultPersistentStore(): MemoryDataStore<LazyDataCacheSave<T>> =
    MemoryDataStore(LazyDataCacheSave.empty())

interface LazyDataCacheContext {
    /**
     * 清空所有本地缓存以及当前的 [PagedSource].
     */
    suspend fun invalidate()
}

@Serializable
class LazyDataCacheSave<T> private constructor(
    val list: List<T> = emptyList(),
    val page: Int? = null,
    val totalSize: Int? = null,
    val time: Long = 0,
    @Suppress("unused")
    private val _version: Int = CURRENT_VERSION,
    // Note: we need default values to make it compatible. otherwise it will crash asDataStoreSerializer.readFrom
) {
    constructor(list: List<T>, page: Int?, totalSize: Int?, time: Long = currentTimeMillis()) :
            this(list, page, totalSize, time, _version = CURRENT_VERSION)

    override fun toString(): String {
        return "LazyDataCacheSave(page=$page, totalSize=$totalSize, time=$time, list=$list)"
    }

    companion object {
        private const val CURRENT_VERSION = 1
        private val Empty = LazyDataCacheSave<Any?>()

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): LazyDataCacheSave<T> = Empty as LazyDataCacheSave<T>
    }
}

// See also `mutablePreferencesOf` from commonTest.
class MemoryDataStore<T>(initial: T) : DataStore<T> {
    override val data: MutableStateFlow<T> = MutableStateFlow(initial)
    private val lock = Mutex()
    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        lock.withLock {
            val newData = transform(data.value)
            data.value = newData
            return newData
        }
    }
}


private class LazyDataCacheImpl<T>(
    private val createSource: suspend LazyDataCacheContext.() -> ApiResponse<PagedSource<T>>,
    private val getKey: (T) -> Any?,
    private val debugName: String? = null,
    // don't call [dataStore.updateData], call [LazyDataCacheImpl.updateDataSanitized] instead
    private val persistentStore: DataStore<LazyDataCacheSave<T>> = defaultPersistentStore(),
) : LazyDataCache<T>, LazyDataCacheContext {
    private val logger = logger(LazyDataCacheImpl::class)

    private sealed class SourceInfo<T> {
        class Success<T>(
            val source: PagedSource<T>?,
        ) : SourceInfo<T>()

        class ApiError<T>(
            val reason: ApiFailure,
        ) : SourceInfo<T>()

        class UnknownError<T>(
            val cause: Throwable,
        ) : SourceInfo<T>()
    }

    private val SourceInfo<T>.sourceOrNull get() = (this as? SourceInfo.Success)?.source

    // Writes must be under lock
    private val currentSourceInfo: MutableStateFlow<SourceInfo<T>?> = MutableStateFlow(null)
    private val currentSource get() = currentSourceInfo.map { (it as? SourceInfo.Success)?.source }
    private val sourceCompleted = currentSource.flatMapLatest { it?.finished ?: flowOf(false) }
    private val persistentData = persistentStore.data.flowOn(Dispatchers.Default) // 别在 UI 算

    override val cachedDataFlow: Flow<List<T>> = persistentData.map {
        it.list
    }
    override val state: Flow<LazyDataCacheState> = currentSourceInfo.map {
        when (it) {
            null,
            is SourceInfo.Success -> LazyDataCacheState.Normal

            is SourceInfo.ApiError -> LazyDataCacheState.ApiError(it.reason)
            is SourceInfo.UnknownError -> LazyDataCacheState.UnknownError(it.cause)
        }
    }

    private val remoteTotalSize = currentSource.transformLatest { source ->
        if (source == null) {
            emit(null)
            return@transformLatest
        }
        emitAll(source.totalSize)
    }

    override val totalSize: Flow<Int?> = combine(persistentData, remoteTotalSize) { save, remote ->
        remote ?: save.totalSize
    }

    /**
     * 更新背后的实际数据. 所有的更新都要调用这里.
     *
     * 必须使用 [lock].
     */
    private suspend inline fun updateDataSanitized(
        orderPolicy: RefreshOrderPolicy,
        allowSavedPage: Boolean = false,
        crossinline block: (List<T>) -> List<T>
    ) {
        // 更新持久的数据, 保证缓存一致性
        persistentStore.updateData { save ->
            val source = currentSource.first()
            when (orderPolicy) {
                RefreshOrderPolicy.REPLACE -> {
                    val newList = block(save.list).fastDistinctBy { getKey(it) }
                    return@updateData LazyDataCacheSave(
                        newList,
                        page = source?.currentPage?.value ?: if (allowSavedPage) null else save.page,
                        totalSize = (source?.totalSize?.value ?: if (allowSavedPage) null else save.totalSize)?.let {
                            it + (newList.size - save.list.size)
                        },
                    )
                }

                RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST -> {
                    val original = save.list
                    val originalKeys = original.mapTo(ArrayList(original.size), getKey)
                    val new = block(original).fastDistinctBy { getKey(it) }
                    val newKeys = new.mapTo(ArrayList(new.size), getKey)

                    val newIndices = Array(new.size) { it }
                    newIndices.sortBy { index ->
                        val originalIndex = originalKeys.indexOf(newKeys[index])
                        if (originalIndex == -1) {
                            Int.MAX_VALUE // not found, put it at the end
                        } else originalIndex
                    }
                    val newList = newIndices.map { new[it] }
                    return@updateData LazyDataCacheSave(
                        newList,
                        page = source?.currentPage?.value ?: if (allowSavedPage) null else save.page,
                        totalSize = (source?.totalSize?.value ?: if (allowSavedPage) null else save.totalSize)?.let {
                            it + (newList.size - save.list.size)
                        },
                    )
//                    
//                    // associateByTo also distinct
//                    val original =
//                        save.list.associateByTo(LinkedHashMap(initialCapacity = save.list.size)) { getKey(it) }
//                    val new = block(save.list).associateByTo(LinkedHashMap()) { getKey(it) }
//
//                    new.entries.sortedBy {
//                        val index = original.keys.indexOf(it)
//                        if (index == -1) {
//                            Int.MAX_VALUE
//                        } else index
//                    }
//                    return@updateData LazyDataCacheSave(
//                        new.values.toList(),
//                        source.currentPage.value,
//                        source.totalSize.value
//                    )
                }
            }
        }
    }

    override val allDataFlow: Flow<List<T>> = channelFlow {
        coroutineScope {
            launch {
                currentSource.onEach {
                    if (it == null) {
                        lock.withLock {
                            getSourceOrCreate(RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST)
                        }
                    }
                }.filterNotNull().collectLatest {
                    while (!it.finished.value) {
                        requestMore()
                    }
                }
            }
            cachedDataFlow.collectLatest {
                send(it)
            }
        }

//        cancellableCoroutineScope {
//            val cached = cachedData.produceIn(this)
//            val sourceRequest = launch {
//                while (!sourceCompleted.first()) {
//                    requestMore()
//                }
//                requestInProgress.filter { !it }.first() // wait for the last request to finish
//            }
//
//            launch {
//                while (isActive) {
//                    select {
//                        sourceRequest.onJoin { // check this first
//                            emit(cachedData.value) // emit the latest value to ensure the last page is emitted
//                            cancelScope()
//                        }
//                        cached.onReceive {
//                            emit(it)
//                        }
//                    }
//                }
//            }
//        }
    }.flowOn(Dispatchers.Default)

    private val requestInProgress = MutableStateFlow(false)

    @OptIn(UnsafeLazyDataCacheApi::class)
    override val lock: Mutex = Mutex()

    @OptIn(UnsafeLazyDataCacheApi::class)
    override val mutator: LazyDataCacheMutator<T> = object : LazyDataCacheMutator<T>() {
        override suspend fun update(map: (List<T>) -> List<T>) { // under lock
            updateDataSanitized(RefreshOrderPolicy.REPLACE) { map(it) }
        }
    }

    @Volatile
    private var firstLoad = true

    override suspend fun requestMore(): Boolean {
        // impl notes:
        // 这函数必须支持 cancellation, 因为它会在 composition 线程调用

        return withContext(Dispatchers.IO) {
            lock.withLock {
                if (firstLoad) {
                    firstLoad = false
                    val save = persistentData.first()
                    logger.info { "Initialize LazyDataCache($debugName) with save $save" }
                    if (save.page != null && save.page != 0) {
                        // We have a page saved, we should restore it
                        val source = getSourceOrCreate(RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST)
                            ?: return@withContext false
                        source.skipToPage(save.page)
                        // fall through to request the next page
                    }
                }

                val source = getSourceOrCreate(RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST)
                    ?: return@withContext false

                try {
                    requestInProgress.value = true
//                    logger.info { "Requesting more data from $source, page=${source.currentPage.value}" }
                    val resp = source.nextPageOrNull() // cancellation-supported
                    if (resp == null) {
                        check(source.finished.value) {
                            "PagedSource.nextPageOrNull() must not return null if the source is not finished."
                        }
                    }
                    return@withContext if (resp != null) {
                        try {
                            updateDataSanitized(RefreshOrderPolicy.REPLACE) { it + resp }
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
    private suspend inline fun LazyDataCacheImpl<T>.getSourceOrCreate(orderPolicy: RefreshOrderPolicy): PagedSource<T>? {
        (currentSourceInfo.value as? SourceInfo.Success)?.source?.let { return it }
        return (try {
            createSource().fold(
                onSuccess = {
                    SourceInfo.Success(it)
                },
                onKnownFailure = {
                    SourceInfo.ApiError(it)
                },
            )
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Throwable) {
            SourceInfo.UnknownError(e)
        }.also {
            currentSourceInfo.value = it
        } as? SourceInfo.Success)?.source
    }

    override suspend fun refresh(orderPolicy: RefreshOrderPolicy): Boolean {
        return lock.withLock {
            withContext(Dispatchers.IO) {
                val source = createSource().getOrNull() ?: return@withContext false // note: always create a new source
                try {
                    requestInProgress.value = true
                    val resp = source.nextPageOrNull() // cancellation-supported

                    // Update source only if the request was successful, as per documentation on [refresh]
                    currentSourceInfo.value = SourceInfo.Success(source)
                    updateDataSanitized(
                        orderPolicy,
                        allowSavedPage = false,
                    ) { resp.orEmpty() } // must after currentSourceInfo update

                    true
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
    override val lastUpdated: Flow<Long> = persistentData.map { it.time }

    override suspend fun invalidate() {
        lock.withLock {
            currentSourceInfo.value = null
            withContext(Dispatchers.Default) {
                persistentStore.updateData {
                    LazyDataCacheSave.empty()
                }
            }
        }
    }
}