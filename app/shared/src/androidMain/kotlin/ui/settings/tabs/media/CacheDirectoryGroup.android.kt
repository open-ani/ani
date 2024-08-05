package me.him188.ani.app.ui.settings.tabs.media

import android.os.Environment
import androidx.annotation.UiThread
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.platform.findActivity
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.RichDialogLayout
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SingleSelectionElement
import me.him188.ani.app.ui.settings.framework.components.SingleSelectionItem
import me.him188.ani.utils.io.resolve
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

private const val DEFAULT_TORRENT_CACHE_DIR_NAME = "torrent-caches"

/**
 * 代表 Android 平台可能可以用来存储 BT 缓存的位置
 */
private sealed interface AndroidTorrentCacheLocation {
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
     * App 外部私有存储，外部存储可能不可用
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
     * @param basePath 授权的公共存储路径，为 `null` 则表示没有授权过外部目录
     * @see android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
     */
    data class ExternalShared(val basePath: String?, val accessible: Boolean) : AndroidTorrentCacheLocation {
        override val name: String = "外部公共目录"
        override val pathPresentation: String get() = basePath ?: "不可用"
        override fun toString(): String {
            return "$name：$pathPresentation"
        }
    }
}

private class AndroidTorrentCacheViewModel(
    private val context: ContextMP,
    private val mediaCacheSettings: AbstractSettingsViewModel.Settings<MediaCacheSettings, MediaCacheSettings>
) : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val permissionManager: PermissionManager by inject()

    private val defaultTorrentCacheDir by lazy {
        val activity = context.findActivity() ?: error("failed to find activity")
        activity.filesDir.resolve(DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath
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

    @UiThread
    suspend fun refreshStorageState() {
        val settings by mediaCacheSettings
        val currentDir = settings.saveDir ?: defaultTorrentCacheDir

        val internalPrivateBasePath = context.filesDir.absolutePath
        val externalPrivateBasePath = context.getExternalFilesDir(null)?.absolutePath
        val externalSharedBasePath = Environment.getExternalStorageDirectory().absolutePath

        val resultList = mutableListOf<SingleSelectionElement<AndroidTorrentCacheLocation>>()

        currentSelection = if (currentDir == null) {
            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.basePath != null))
            resultList.add(SingleSelectionElement(getAccessibleExternalSharedLocation(), true))

            null
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
            // 设置保存的是外部公共目录
            val accessible = permissionManager.getExternalManageableDocumentPermission(context, Path((currentDir)))
            val value = AndroidTorrentCacheLocation.ExternalShared(currentDir, accessible)

            resultList.add(SingleSelectionElement(constructInternalPrivateLocation(), true))
            val externalPrivate = getExternalPrivateLocation()
            resultList.add(SingleSelectionElement(externalPrivate, externalPrivate.basePath != null))
            resultList.add(SingleSelectionElement(getAccessibleExternalSharedLocation(), true))

            value
        } else {
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
            is AndroidTorrentCacheLocation.ExternalShared -> location.basePath
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
        val cacheVm = rememberViewModel { AndroidTorrentCacheViewModel(context, vm.mediaCacheSettings) }
        val loading = vm.mediaSelectorSettings.loading

        LaunchedEffect(key1 = loading) {
            if (!loading) {
                cacheVm.refreshStorageState()
            }
        }

        SingleSelectionItem(
            title = { Text("BT 视频缓存位置") },
            dialogDescription = {
                Text(
                    "选择外部目录将允许其他应用读取 BT 视频缓存。\n" +
                            "对于外部共享目录，你需要手动为 Ani 授权某个目录的读写权限。\n" +
                            "更改存储目录后需要手动迁移旧存储数据。",
                )
            },
            modifier = Modifier.placeholder(loading),
            items = cacheVm.torrentLocationPresentation,
            selected = cacheVm.currentSelectionIndex,
            key = { it.name },
            value = { Text(text = it?.toString() ?: "未知目录") },
            listItem = {
                ItemHeader(
                    title = { Text(text = it.name) },
                    description = { Text(text = it.pathPresentation) },
                )
            },
            onOpenDialog = {
                scope.launch { cacheVm.refreshStorageState() }
            },
            onSelectItem = {
                if (it !is AndroidTorrentCacheLocation.ExternalShared || it.accessible) {
                    return@SingleSelectionItem true
                }
                val deferred = CompletableDeferred<Boolean>()
                selectExternalSharedStorageRequest = deferred
                showAlertDialog = true
                val selectRequest = deferred.await()
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
            onConfirm = {
                scope.launch { cacheVm.setStorage(it) }
                toaster.toast("BT 视频缓存位置变更在重启后生效。")
            },
        )
    }

    if (showAlertDialog) {
        BasicAlertDialog(onDismissRequest = { selectExternalSharedStorageRequest?.complete(false) }) {
            RichDialogLayout(
                title = { Text("授予 Ani 外部共享存储权限") },
                buttons = {
                    TextButton({ selectExternalSharedStorageRequest?.complete(false) }) { Text("取消") }
                    Button({ selectExternalSharedStorageRequest?.complete(true) }) { Text("确认") }
                },
            ) {
                Text("将 BT 视频缓存位置设置为外部共享存储之前需要授予 Ani 对应的目录访问权限。")
                Text("Ani 将仅拥有您授予的目录及其子目录的读写权限，您的其他文件对于 Ani 来说不可访问。")
                Text("点击确定后将打开文件选择器，请选择一个目录并授予 Ani 权限。")
            }
        }
    }
    
}