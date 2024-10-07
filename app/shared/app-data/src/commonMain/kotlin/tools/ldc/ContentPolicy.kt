/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.tools.ldc

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.tools.ldc.ContentPolicy.CACHE_FIRST
import me.him188.ani.app.tools.ldc.ContentPolicy.CACHE_ONLY


/**
 * 内容获取策略. 使用 [CACHE_FIRST] 可以保证获取到所有的数据, 但可能会破坏惰性请求的特性. (在手机上我们希望尽可能减少请求次数)
 * 一般优先使用 [CACHE_ONLY], 只在期望获取全部数据时才使用 [CACHE_FIRST].
 */
enum class ContentPolicy { // TODO: remove this, we always need CACHE_FIRST
    /**
     * 只读取缓存而不会请求网络, 若缓存中不包含所需数据, 则对应函数或 flow 可能会返回空.
     */
    CACHE_ONLY,

    /**
     * 缓存优先策略. 若本地已经有缓存, 则不会请求网络.
     */
    @Deprecated("这东西现在会贪婪请求全部列表, 导致大量请求, 要先修一下") // TODO
    CACHE_FIRST,
}

/**
 * 根据 [ContentPolicy], 选择仅使用已缓存数据 [LazyDataCache.cachedDataFlow] 还是使用允许网络请求的所有数据 [LazyDataCache.allDataFlow].
 */
@Suppress("DEPRECATION")
fun <T> LazyDataCache<T>.data(policy: ContentPolicy): Flow<List<T>> = when (policy) {
    CACHE_ONLY -> cachedDataFlow
    CACHE_FIRST -> allDataFlow
}
