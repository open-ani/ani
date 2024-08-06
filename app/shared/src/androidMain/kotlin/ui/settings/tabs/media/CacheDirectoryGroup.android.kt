package me.him188.ani.app.ui.settings.tabs.media

import android.os.Environment
import androidx.annotation.UiThread
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SingleSelectionElement
import me.him188.ani.app.ui.settings.framework.components.SingleSelectionItem
import me.him188.ani.utils.io.resolve
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

const val DEFAULT_TORRENT_CACHE_DIR_NAME = "torrent-caches"

/**
 * 代表 Android 平台可能可以用来存储 BT 缓存的位置
 */
sealed interface AndroidTorrentCacheLocation {
    val name: String
    val pathPresentation: String

    /**
     * App 内部私有存储总是可用
     *
     * @param basePath App 私有存储的根路径
     * @see android.content.Context.getFilesDir
     */
    data class InternalPrivate(val basePath: String) : AndroidTorrentCacheLocation {
        override val name: String = "内部私有目录"
        override val pathPresentation: String get() = basePath
        override fun toString(): String {
            return "$name：$basePath"
        }
    }

    /**
     * App 外部私有存储，外部存储几乎不可能不可用，除非是外置的 SD 卡并且已经移除。现在几乎所有的外部目录都是模拟的
     *
     * @param basePath App 外部私有存储的根路径，为 `null` 则表示共享外部存储设备不可用
     * @see android.content.Context.getExternalFilesDir
     */
    data class ExternalPrivate(val basePath: String?) : AndroidTorrentCacheLocation {
        override val name: String = "外部私有目录"
        override val pathPresentation: String get() = basePath ?: "不可用"
        override fun toString(): String {
            return "$name：$pathPresentation"
        }
    }

    /**
     * 通过 [android.provider.DocumentsProvider] 获取的授权路径
     *
     * @param path 授权的共享存储路径，为 `null` 则表示没有授权过外部目录
     * @see android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
     */
    data class ExternalShared(val path: String?, val accessible: Boolean) : AndroidTorrentCacheLocation {
        override val name: String = "外部共享目录"
        override val pathPresentation: String
            get() = when {
                path == null -> "点击授权"
                !accessible -> "不可用，点击重新授权\n$path"
                else -> path
            }
        override fun toString(): String {
            return "$name：$pathPresentation"
        }
    }
}

interface AndroidEnvironment {
    fun getExternalStorageDirectory(): File
}

private object DefaultAndroidEnvironment : AndroidEnvironment {
    override fun getExternalStorageDirectory(): File {
        return Environment.getExternalStorageDirectory()
    }
}

class AndroidTorrentCacheViewModel(
    private val context: ContextMP,
    private val mediaCacheSettings: AbstractSettingsViewModel.Settings<MediaCacheSettings, MediaCacheSettings>,
    private val permissionManager: PermissionManager,
    private val environment: AndroidEnvironment = DefaultAndroidEnvironment // allow mock
) : AbstractSettingsViewModel() {
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
        torrentLocationPresentation.forEachIndexed { i, e ->
            if (e.value.name == current.name) {
                return@derivedStateOf i
            }
        }
        -1
    }

    /**
     * 获取 [`内部私有存储`][android.content.Context.getFilesDir], [`外部私有存储`][android.content.Context.getExternalFilesDir] 和
     * [`已授权的外部共享存储`][android.content.ContentResolver.getPersistedUriPermissions] 的可用状态, 根据 [MediaCacheSettings.saveDir]
     * 判断当前的的选择，并更新至 [torrentLocationPresentation] 和 [currentSelection] UI 状态。
     *
     * @see android.content.Context.getFilesDir 内部私有存储
     * @see android.content.Context.getExternalFilesDir 外部私有存储
     * @see android.content.ContentResolver.getPersistedUriPermissions 获取已授权给 App 的资源位置，包括外部共享存储位置
     */
    @UiThread
    suspend fun refreshStorageState() {
        val settings by mediaCacheSettings
        val currentDir = settings.saveDir ?: defaultTorrentCacheDir

        val internalPrivateBasePath = context.filesDir.absolutePath
        val externalPrivateBasePath = context.getExternalFilesDir(null)?.absolutePath
        val externalSharedBasePath = environment.getExternalStorageDirectory().absolutePath

        val resultList = mutableListOf<SingleSelectionElement<AndroidTorrentCacheLocation>>()

        currentSelection = if (currentDir == null) {
            // settings preference 设定的保存目录为空，这是不可能的
            // 因为在 Android 端启动时会默认设置为内部私有目录
            val value = constructInternalPrivateLocation()
            resultList.add(SingleSelectionElement(value, true))
            val externalPrivate = getExternalPrivateLocation()
            // 如果外部私有存储不可用，则选择列表中将不允许选择这一项
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.basePath != null))
            resultList.add(SingleSelectionElement(getAccessibleExternalSharedLocation(), true))

            value
        } else if (currentDir.startsWith(internalPrivateBasePath)) {
            // 设置保存的是内部私有目录
            val value = AndroidTorrentCacheLocation.InternalPrivate(currentDir)
            resultList.add(SingleSelectionElement(value, true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.basePath != null))
            resultList.add(SingleSelectionElement(getAccessibleExternalSharedLocation(), true))

            value
        } else if (externalPrivateBasePath != null && currentDir.startsWith(externalPrivateBasePath)) {
            // 设置保存的是外部私有目录
            val value = AndroidTorrentCacheLocation.ExternalPrivate(currentDir)

            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            resultList.add(SingleSelectionElement(value, true))
            resultList.add(SingleSelectionElement(getAccessibleExternalSharedLocation(), true))

            value
        } else if (currentDir.startsWith(externalSharedBasePath)) {
            // 设置保存的是外部共享目录
            val accessible = permissionManager.getExternalManageableDocumentPermission(context, Path((currentDir)))
            val value = AndroidTorrentCacheLocation.ExternalShared(currentDir, accessible)

            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.basePath != null))
            resultList.add(SingleSelectionElement(value, true))

            value
        } else {
            // 不是上面三种存储目录的任何一种，不可能发生
            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.basePath != null))
            resultList.add(SingleSelectionElement(getAccessibleExternalSharedLocation(), true))

            null
        }

        torrentLocationPresentation = resultList
    }

    @UiThread
    suspend fun setStorage(location: AndroidTorrentCacheLocation) {
        val targetPath = when (location) {
            is AndroidTorrentCacheLocation.InternalPrivate -> location.basePath
            is AndroidTorrentCacheLocation.ExternalPrivate -> location.basePath
            is AndroidTorrentCacheLocation.ExternalShared -> location.path
        }

        if (targetPath == null) {
            // failed to set
            return
        }

        val settings by mediaCacheSettings
        mediaCacheSettings.updateSuspended(settings.copy(saveDir = targetPath))

        refreshStorageState()
    }

    @UiThread
    suspend fun requestExternalSharedStorage(): String? {
        return permissionManager.requestExternalManageableDocument(context)?.toString()
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

    /**
     * 尝试获取一个已经授权的外部共享目录
     */
    private suspend fun getAccessibleExternalSharedLocation(): AndroidTorrentCacheLocation.ExternalShared {
        val externalSharedBase = permissionManager.getAccessibleExternalManageableDocumentPath(context)
        return AndroidTorrentCacheLocation.ExternalShared(
            externalSharedBase?.resolve(DEFAULT_TORRENT_CACHE_DIR_NAME)?.toString(), externalSharedBase != null,
        )
    }
}

@Composable
actual fun SettingsScope.CacheDirectoryGroup(vm: MediaSettingsViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toaster = LocalToaster.current

    var showAlertDialog by rememberSaveable { mutableStateOf(false) }
    var selectExternalSharedStorageRequest: CompletableDeferred<Boolean>? by remember { mutableStateOf(null) }

    Group({ Text("存储设置") }) {
        val cacheVm = rememberViewModel {
            AndroidTorrentCacheViewModel(context, vm.mediaCacheSettings, getKoin().get())
        }
        val loading = vm.mediaSelectorSettings.loading

        LaunchedEffect(key1 = loading) {
            if (!loading) {
                cacheVm.refreshStorageState()
            }
        }

        SingleSelectionItem(
            title = { Text("BT 视频缓存位置") },
            description = { Text(text = it?.toString() ?: "未知目录") },
            modifier = Modifier.placeholder(loading),
            items = cacheVm.torrentLocationPresentation,
            dialogDescription = {
                Column {
                    Text("选择外部目录将允许其他应用读取 BT 视频缓存。")
                    Text("对于外部共享目录，你需要手动为 Ani 授权某个目录的读写权限。")
                    Text("更改存储目录后需要手动迁移旧存储数据。")
                }
            },
            dialogIcon = { Icon(imageVector = Icons.Default.Storage, contentDescription = null) },
            selected = cacheVm.currentSelectionIndex,
            key = { it.name },
            listItem = {
                ItemHeader(
                    title = { Text(text = it.name) },
                    description = { Text(text = it.pathPresentation) },
                )
            },
            onOpenDialog = {
                scope.launch { cacheVm.refreshStorageState() }
            },
            onSelectItem = { location ->
                if (location !is AndroidTorrentCacheLocation.ExternalShared || location.accessible) {
                    return@SingleSelectionItem true
                }
                val deferred = CompletableDeferred<Boolean>()
                selectExternalSharedStorageRequest = deferred
                showAlertDialog = true
                val selectRequest = try {
                    deferred.await()
                } catch (_: CancellationException) {
                    false
                }
                showAlertDialog = false

                if (selectRequest) {
                    val grantDir = cacheVm.requestExternalSharedStorage()
                    if (grantDir == null) {
                        toaster.toast("未选择任何目录并授予权限。")
                        false
                    } else {
                        cacheVm.refreshStorageState()
                        true
                    }
                } else {
                    false
                }
            },
            onConfirm = { location ->
                scope.launch { location?.let { cacheVm.setStorage(it) } }
                toaster.toast("BT 视频缓存位置变更在重启后生效。")
            },
        )
    }

    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { selectExternalSharedStorageRequest?.complete(false) },
            confirmButton = {
                Button({ selectExternalSharedStorageRequest?.complete(true) }) { Text("确认") }
            },
            dismissButton = {
                TextButton({ selectExternalSharedStorageRequest?.complete(false) }) { Text("取消") }
            },
            title = { Text("授予 Ani 外部共享存储权限") },
            text = {
                Column {
                    Text("将 BT 视频缓存位置设置为外部共享存储之前需要授予 Ani 对应的存储访问权限。")
                    Text("为了你的数据安全，Ani 将仅仅申请外部存储的其中一个目录的访问权限，你的其他文件将不会被 Ani 读取。")
                    Text("点击确定后将打开文件选择器，请选择一个目录并授予 Ani 权限。")
                }
            },
        )
    }
}