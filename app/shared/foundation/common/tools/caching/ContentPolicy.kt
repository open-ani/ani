package me.him188.ani.app.tools.caching

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.tools.caching.ContentPolicy.CACHE_FIRST
import me.him188.ani.app.tools.caching.ContentPolicy.CACHE_ONLY


/**
 * 内容获取策略. 使用 [CACHE_FIRST] 可以保证获取到所有的数据, 但可能会破坏惰性请求的特性. (在手机上我们希望尽可能减少请求次数)
 * 一般优先使用 [CACHE_ONLY], 只在期望获取全部数据时才使用 [CACHE_FIRST].
 */
enum class ContentPolicy {
    /**
     * 只读取缓存而不会请求网络, 若缓存中不包含所需数据, 则对应函数或 flow 可能会返回空.
     */
    CACHE_ONLY,

    /**
     * 缓存优先策略. 若本地已经有缓存, 则不会请求网络.
     */
    CACHE_FIRST,
}

/**
 * 根据 [ContentPolicy], 选择仅使用已缓存数据 [LazyDataCache.cachedDataFlow] 还是使用允许网络请求的所有数据 [LazyDataCache.allDataFlow].
 */
fun <T> LazyDataCache<T>.data(policy: ContentPolicy): Flow<List<T>> = when (policy) {
    CACHE_ONLY -> cachedDataFlow
    CACHE_FIRST -> allDataFlow
}
