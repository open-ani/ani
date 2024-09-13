package me.him188.ani.app.data.persistent

import me.him188.ani.app.platform.Context


/**
 * Must not be stored
 */
expect val Context.dataStoresImpl: PlatformDataStoreManager

// workaround for compiler bug
inline val Context.dataStores: PlatformDataStoreManager get() = dataStoresImpl
