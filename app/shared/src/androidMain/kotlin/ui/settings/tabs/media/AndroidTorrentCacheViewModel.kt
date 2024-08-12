package me.him188.ani.app.ui.settings.tabs.media

import androidx.annotation.UiThread
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.components.SingleSelectionElement
import java.io.File

const val DEFAULT_TORRENT_CACHE_DIR_NAME = "torrent-caches"

class AndroidTorrentCacheViewModel(
    private val context: ContextMP,
    private val mediaCacheSettings: AbstractSettingsViewModel.Settings<MediaCacheSettings, MediaCacheSettings>,
    private val permissionManager: PermissionManager,
) : AbstractViewModel() {
    private val defaultTorrentCacheDir by lazy {
        context.filesDir.resolve(DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath
    }

    var torrentLocationPresentation: List<SingleSelectionElement<AndroidTorrentCacheLocation>>
            by mutableStateOf(emptyList())
        private set
    var currentSelection: AndroidTorrentCacheLocation? by mutableStateOf(null)
        private set
    val currentSelectionIndex by derivedStateOf {
        val current = currentSelection ?: return@derivedStateOf -1
        torrentLocationPresentation.indexOfFirst { it.value == current }
    }

    /**
     * 申请外部共享目录授权时需要弹出对话框等待用户的回应
     */
    private var selectExternalSharedStorageRequest: CompletableDeferred<Boolean>? by mutableStateOf(null)
    val showExternalSharedStorageRequestDialog by derivedStateOf { selectExternalSharedStorageRequest != null }

    /**
     * 获取 [`内部私有存储`][android.content.Context.getFilesDir] 和 [`外部私有存储`][android.content.Context.getExternalFilesDir]
     * 的可用状态, 根据 [MediaCacheSettings.saveDir] 判断当前的的选择，并更新至 [torrentLocationPresentation] 和 [currentSelection] UI 状态。
     *
     * @see android.content.Context.getFilesDir 内部私有存储
     * @see android.content.Context.getExternalFilesDir 外部私有存储
     */
    @UiThread
    fun refreshStorageState() {
        val settings by mediaCacheSettings
        val currentDir = settings.saveDir ?: defaultTorrentCacheDir

        val internalPrivateBasePath = context.filesDir.absolutePath
        val externalPrivateBasePath = context.getExternalFilesDir(null)?.absolutePath

        val resultList = mutableListOf<SingleSelectionElement<AndroidTorrentCacheLocation>>()

        currentSelection = if (currentDir == null) {
            // settings preference 设定的保存目录为空，这是不可能的
            // 因为在 Android 端启动时会默认设置为内部私有目录
            val value = constructInternalPrivateLocation()
            resultList.add(SingleSelectionElement(value, true))
            val externalPrivate = getExternalPrivateLocation()
            // 如果外部私有存储不可用，则选择列表中将不允许选择这一项
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.path != null))

            value
        } else if (currentDir.startsWith(internalPrivateBasePath)) {
            // 设置保存的是内部私有目录
            val value = AndroidTorrentCacheLocation.InternalPrivate(currentDir)
            resultList.add(SingleSelectionElement(value, true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.path != null))

            value
        } else if (externalPrivateBasePath != null && currentDir.startsWith(externalPrivateBasePath)) {
            // 设置保存的是外部私有目录
            val value = AndroidTorrentCacheLocation.ExternalPrivate(currentDir)

            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            resultList.add(SingleSelectionElement(value, true))

            value
        } else {
            // 不是上面三种存储目录的任何一种，不可能发生
            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.path != null))

            null
        }

        torrentLocationPresentation = resultList
    }

    @UiThread
    suspend fun setStorage(location: AndroidTorrentCacheLocation) {
        val targetPath = when (location) {
            is AndroidTorrentCacheLocation.InternalPrivate -> location.path
            is AndroidTorrentCacheLocation.ExternalPrivate -> location.path
        }

        if (targetPath == null) {
            // failed to set
            return
        }

        val settings by mediaCacheSettings
        mediaCacheSettings.updateSuspended(settings.copy(saveDir = targetPath))

        refreshStorageState()
    }

    /**
     * 申请一个可以访问的外部 uri
     *
     * @return `null` 表示取消申请权限，空字符串表示未在 DocumentUI 授权任何目录
     */
    @UiThread
    suspend fun requestExternalDocumentTree(): String? {
        selectExternalSharedStorageRequest?.cancel() // cancel existing request job

        val deferred = CompletableDeferred<Boolean>()
        selectExternalSharedStorageRequest = deferred

        val decision = deferred.await()
        selectExternalSharedStorageRequest = null

        return if (decision) {
            permissionManager.requestExternalDocumentTree(context)
        } else {
            null
        }
    }

    @UiThread
    fun respondSelectExternalSharedStorage(allow: Boolean) {
        selectExternalSharedStorageRequest?.complete(allow)
    }

    /**
     * 获取内部私有目录
     */
    private fun constructInternalPrivateLocation(): AndroidTorrentCacheLocation.InternalPrivate {
        return AndroidTorrentCacheLocation.InternalPrivate(defaultTorrentCacheDir)
    }

    /**
     * 尝试获取外部私有目录
     */
    private fun getExternalPrivateLocation(): AndroidTorrentCacheLocation.ExternalPrivate {
        val externalPrivateBasePath = context.getExternalFilesDir(null)?.absolutePath
        return AndroidTorrentCacheLocation.ExternalPrivate(
            externalPrivateBasePath?.let { File(it).resolve(DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath },
        )
    }
}