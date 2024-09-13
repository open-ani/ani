package me.him188.ani.app.ui.settings.tabs.media

/**
 * 代表 Android 平台可能可以用来存储 BT 缓存的位置
 */
sealed interface AndroidTorrentCacheLocation {
    /**
     * App 内部私有存储总是可用
     *
     * @param path 缓存的内部私有存储的路径
     * @see android.content.Context.getFilesDir
     */
    data class InternalPrivate(val path: String) : AndroidTorrentCacheLocation

    /**
     * App 外部私有存储，外部存储几乎不可能不可用，除非是外置的 SD 卡并且已经移除。现在几乎所有的外部目录都是模拟的
     *
     * @param path 缓存的外部私有存储的路径，为 `null` 则表示共享外部存储设备不可用
     * @see android.content.Context.getExternalFilesDir
     */
    data class ExternalPrivate(val path: String?) : AndroidTorrentCacheLocation
}