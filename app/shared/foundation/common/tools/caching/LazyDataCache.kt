package me.him188.ani.app.tools.caching

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn

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
     * Whether the current remote flow has been exhausted.
     *
     * Note that when [isCompleted] is seen as `true`, it can still become `false` later if the remote source has restarted (in which case [data] will be cleared).
     */
    val isCompleted: Flow<Boolean>

    /**
     * Current cache of the data. It can change if [requestMore].
     */
    val data: StateFlow<List<T>>

    /**
     * Total size of the data. It can change if [requestMore].
     *
     * It can be `null` if not known.
     */
    val totalSize: Flow<Int?>

    val lock: Mutex

    /**
     * Changes the [data] under the [lock].
     *
     * Note, you must not call [mutate] again within [action], as it will cause a deadlock.
     */
    suspend fun mutate(action: suspend List<T>.() -> List<T>)

    /**
     * Attempts to load more data.
     * Returns immediately if the remote flow has already been exhausted (completed).
     * Returns when the next page is loaded.
     *
     * The will only be one data loading operation at a time.
     *
     * This function performs calculations on the [Dispatchers.Default] dispatcher.
     */
    suspend fun requestMore()
}

inline val <T> LazyDataCache<T>.value get() = data.value

/**
 * Creates a [LazyDataCache].
 *
 * On [LazyDataCache.requestMore], the [nextPageOrNull] will be called to load more data.
 * If [nextPageOrNull] returns `null`, it is considered to be end of the data. [LazyDataCache] will release the reference to [nextPageOrNull].
 *
 * `nextPageOrNull` must not throw any exceptions, if it does, it is considered to be end of the data.
 *
 * Completion of [LazyDataCache] relies on the completion of the [nextPageOrNull] flow.
 */
fun <T> LazyDataCache(
    nextPageOrNull: Flow<PagedSource<T>>,
    debugName: String? = null,
): LazyDataCache<T> = LazyDataCacheImpl(nextPageOrNull, debugName)

/**
 * @see LazyDataCache
 */
fun <T> LazyDataCache(
    nextPageOrNull: PagedSource<T>,
    debugName: String? = null,
): LazyDataCache<T> = LazyDataCacheImpl(flowOf(nextPageOrNull), debugName)

fun <T> Flow<PagedSource<T>>.cached(
    debugName: String? = null,
): LazyDataCache<T> = LazyDataCache(this, debugName)

fun <T> PagedSource<T>.cached(
    debugName: String? = null,
): LazyDataCache<T> = LazyDataCache(this, debugName)


class LazyDataCacheImpl<T>(
    source: Flow<PagedSource<T>>,
    private val debugName: String? = null,
) : LazyDataCache<T> {
    override val data: MutableStateFlow<List<T>> = MutableStateFlow(emptyList())

    private val scope =
        CoroutineScope(Dispatchers.Default) // Note that a job is not needed as we rely on the [source]'s completion to complete our [currentSource].

    private val sourceCompleted = MutableStateFlow(false)
    private val requestInProgress = MutableStateFlow(false)

    override val lock: Mutex = Mutex()

    private val currentSource: StateFlow<PagedSource<T>?> =
        source.onEach {
            lock.withLock {
                logger.info { "LazyDataCacheImpl($debugName): source changed, clearing all cached data (previous size = ${data.value.size})" }
                data.value = emptyList()
            }
        }.onCompletion {
            sourceCompleted.value = true
        }.stateIn(scope, SharingStarted.Eagerly, null)
    override val totalSize: Flow<Int?> = currentSource.transformLatest { source ->
        if (source == null) {
            emit(null)
            return@transformLatest
        }
        emitAll(source.totalSize)
    }

    override suspend fun requestMore() {
        if (!scope.isActive) {
            if (logger.isWarnEnabled) {
                logger.warn(IllegalStateException()) {
                    "requestMore called after the cache is closed"
                }
            }
        }

        withContext(Dispatchers.IO) {
            val source = currentSource.filterNotNull().firstOrNull() // this call must be out of lock
                ?: return@withContext // source is empty

            // Get exclusive access before fetching the next page, because we cannot re-fetch the page
            lock.withLock {
                try {
                    requestInProgress.value = true
                    val resp = source.nextPageOrNull()
                    if (resp != null) {
                        data.value += resp
                    }
                } finally {
                    requestInProgress.value = false
                }
            }
        }
    }

    override suspend fun mutate(action: suspend List<T>.() -> List<T>) {
        lock.withLock {
            data.value = action(data.value)
        }
    }

    override val isCompleted =
        combine(requestInProgress, sourceCompleted, currentSource) { requestInProgress, sourceCompleted, source ->
            source?.finished ?: flowOf(sourceCompleted && !requestInProgress)
        }.flatMapLatest { it }

    private companion object {
        val logger = logger(LazyDataCacheImpl::class)
    }
}