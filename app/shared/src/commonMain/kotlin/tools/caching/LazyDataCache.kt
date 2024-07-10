package me.him188.ani.app.tools.caching

import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastDistinctBy
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

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
    suspend fun refresh(orderPolicy: RefreshOrderPolicy)

    /**
     * 清空所有本地缓存以及当前的 [PagedSource]. 注意, 本函数不会请求数据. 即当函数返回时, [cachedDataFlow] 为空.
     */
    suspend fun invalidate()
}

enum class RefreshOrderPolicy {
    /**
     * 尽量保持原有的顺序, 新的物品出现在底部.
     */
    KEEP_ORDER_APPEND_LAST,

    /**
     * 不保持原有顺序, 按照新的数据顺序排列.
     */
    REPLACE,
}

/**
 * 线程安全地修改 [LazyDataCache.cachedDataFlow].
 *
 * @param action 在锁里执行的操作, 其中不可以重复调用 [mutate] 或 [dataTransaction].
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
@OverloadResolutionByLambdaReturnType
fun <T> LazyDataCache(
    createSource: suspend LazyDataCacheContext.() -> PagedSource<T>,
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
    constructor(list: List<T>, page: Int?, totalSize: Int?, time: Long = System.currentTimeMillis()) :
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
    private val createSource: suspend LazyDataCacheContext.() -> PagedSource<T>,
    private val getKey: (T) -> Any?,
    private val debugName: String? = null,
    // don't call [dataStore.updateData], call [LazyDataCacheImpl.updateDataSanitized] instead
    private val persistentStore: DataStore<LazyDataCacheSave<T>> = defaultPersistentStore(),
) : LazyDataCache<T>, LazyDataCacheContext {
    private val logger = logger(LazyDataCacheImpl::class)

    private class SourceInfo<T>(
        val source: PagedSource<T>,
    )

    // Writes must be under lock
    private val currentSourceInfo: MutableStateFlow<SourceInfo<T>?> = MutableStateFlow(null)
    private val currentSource get() = currentSourceInfo.map { it?.source }
    private val sourceCompleted = currentSource.flatMapLatest { it?.finished ?: flowOf(false) }
    private val persistentData = persistentStore.data.flowOn(Dispatchers.Default) // 别在 UI 算

    override val cachedDataFlow: Flow<List<T>> = persistentData.map {
        it.list
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
        crossinline block: (List<T>) -> List<T>
    ) {
        // 更新持久的数据, 保证缓存一致性
        persistentStore.updateData { save ->
            val sourceInfo = currentSourceInfo.value
            when (orderPolicy) {
                RefreshOrderPolicy.REPLACE -> {
                    val source = sourceInfo?.source
                    val newList = block(save.list).fastDistinctBy { getKey(it) }
                    return@updateData LazyDataCacheSave(
                        newList,
                        page = source?.currentPage?.value ?: save.page,
                        totalSize = (source?.totalSize?.value ?: save.totalSize)?.let {
                            it + (newList.size - save.list.size)
                        },
                    )
                }

                RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST -> {
                    val source = sourceInfo?.source

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
                        page = source?.currentPage?.value ?: save.page,
                        totalSize = (source?.totalSize?.value ?: save.totalSize)?.let {
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
                        source.skipToPage(save.page)
                        // fall through to request the next page
                    }
                }

                val source = getSourceOrCreate(RefreshOrderPolicy.KEEP_ORDER_APPEND_LAST)

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
    private suspend inline fun LazyDataCacheImpl<T>.getSourceOrCreate(orderPolicy: RefreshOrderPolicy): PagedSource<T> {
        currentSourceInfo.value?.source?.let { return it }
        return createSource().also {
            currentSourceInfo.value = SourceInfo(it)
        }
    }

    override suspend fun refresh(orderPolicy: RefreshOrderPolicy) {
        lock.withLock {
            withContext(Dispatchers.IO) {
                val source = createSource() // note: always create a new source
                try {
                    requestInProgress.value = true
                    val resp = source.nextPageOrNull() // cancellation-supported

                    // Update source only if the request was successful, as per documentation on [refresh]
                    currentSourceInfo.value = SourceInfo(source)
                    updateDataSanitized(orderPolicy) { resp.orEmpty() } // must after currentSourceInfo update
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
                updateDataSanitized(RefreshOrderPolicy.REPLACE) { emptyList() }
            }
        }
    }
}