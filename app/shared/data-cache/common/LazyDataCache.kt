package me.him188.ani.app.tools.caching

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.api.PagedSource
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn

/**
 * A data collection, where the data is loaded from a remote source.
 *
 * The data is loaded lazily, i.e. only if [requestMore] is called.
 */
interface LazyDataCache<T> : AutoCloseable {
    /**
     * Whether the current remote flow has been exhausted.
     *
     * Note that when [isCompleted] is seen as `true`, it can still become `false` later if the remote source has restarted (in which case [data] will be cleared).
     */
    @Stable
    val isCompleted: Flow<Boolean>

    /**
     * Current cache of the data. It can change if [requestMore].
     */
    @Stable
    val data: StateFlow<List<T>>

    @Stable
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
 */
fun <T> LazyDataCache(
    nextPageOrNull: Flow<PagedSource<T>>,
): LazyDataCache<T> = LazyDataCacheImpl(nextPageOrNull)

fun <T> LazyDataCache(
    nextPageOrNull: PagedSource<T>,
): LazyDataCache<T> = LazyDataCacheImpl(flowOf(nextPageOrNull))

fun <T> Flow<PagedSource<T>>.cached(): LazyDataCache<T> = LazyDataCache(this)

fun <T> PagedSource<T>.cached(): LazyDataCache<T> = LazyDataCache(this)


class LazyDataCacheImpl<T>(
    source: Flow<PagedSource<T>>,
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
                data.value = emptyList()
            }
        }.onCompletion {
            sourceCompleted.value = true
        }.stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun requestMore() {
        if (!scope.isActive) {
            if (logger.isWarnEnabled) {
                logger.warn(IllegalStateException()) {
                    "requestMore called after the cache is closed"
                }
            }
        }

        withContext(Dispatchers.IO) {
            val nextScope = this
            val daemon = launch {
                sourceCompleted.filter { it }.first()
                nextScope.cancel() // stop when the source is completed
            }
            try {
                requestInProgress.value = true
                val source = currentSource.filterNotNull().first()
                val resp = source.nextPageOrNull()
                if (resp != null) {
                    logger.warn("wait for lock")
                    lock.withLock {
                        logger.warn("got lock")
                        data.value += resp
                    }
                }
            } finally {
                requestInProgress.value = false
                daemon.cancel()
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

    override fun close() {
        scope.cancel()
    }

    private companion object {
        val logger = logger(LazyDataCacheImpl::class)
    }
}