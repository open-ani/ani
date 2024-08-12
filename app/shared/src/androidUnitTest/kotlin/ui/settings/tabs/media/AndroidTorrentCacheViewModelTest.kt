package me.him188.ani.app.ui.settings.tabs.media

import android.content.Context
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import kotlinx.io.files.Path
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private typealias MediaCacheSettingsDelegation =
        AbstractSettingsViewModel.Settings<MediaCacheSettings, MediaCacheSettings>

private val INTERNAL_PRIVATE_BASE by lazy { File("/data/data/me.him188.ani/files").absolutePath }
private val EXTERNAL_PRIVATE_BASE by lazy {
    File("/storage/emulated/0/Android/data/me.him188.ani/files").absolutePath
}

class AndroidTorrentCacheViewModelTest : KoinTest {
    @Test
    fun `test default storage location`() = runComposeStateTest {
        val context = createMockContext(externalAvailable = true)
        val permissionManager = createTestPermissionManager(null)
        // 默认情况下，MediaCacheSettings.saveDir 在 App 启动时被赋值为内部私有存储路径
        val mediaCacheSettings by createMockMediaCacheSettingsDelegation()

        assertTrue { mediaCacheSettings.value.saveDir?.startsWith(INTERNAL_PRIVATE_BASE) == true }

        val vm = AndroidTorrentCacheViewModel(context, mediaCacheSettings, permissionManager)
        vm.refreshStorageState()
        takeSnapshot()

        assertEquals(
            AndroidTorrentCacheLocation.InternalPrivate(
                File(INTERNAL_PRIVATE_BASE, DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath,
            ),
            vm.currentSelection,
        )

        val locationPresentation = vm.torrentLocationPresentation
        assertEquals(3, locationPresentation.size)
        assertNotNull(locationPresentation.find { it.value is AndroidTorrentCacheLocation.InternalPrivate })
        assertNotNull(
            locationPresentation.find {
                it.value is AndroidTorrentCacheLocation.ExternalPrivate && it.enabled
            },
        )
    }

    @Test
    fun `test default storage location with external unavailable`() = runComposeStateTest {
        val context = createMockContext(externalAvailable = false)
        val permissionManager = createTestPermissionManager(null)
        // 默认情况下，MediaCacheSettings.saveDir 在 App 启动时被赋值为内部私有存储路径
        val mediaCacheSettings by createMockMediaCacheSettingsDelegation()
        assertTrue { mediaCacheSettings.value.saveDir?.startsWith(INTERNAL_PRIVATE_BASE) == true }

        val vm = AndroidTorrentCacheViewModel(context, mediaCacheSettings, permissionManager)
        vm.refreshStorageState()
        takeSnapshot()

        assertEquals(
            AndroidTorrentCacheLocation.InternalPrivate(
                File(INTERNAL_PRIVATE_BASE, DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath,
            ),
            vm.currentSelection,
        )

        val locationPresentation = vm.torrentLocationPresentation
        assertEquals(3, locationPresentation.size)
        assertNotNull(locationPresentation.find { it.value is AndroidTorrentCacheLocation.InternalPrivate })
        assertNotNull(
            locationPresentation.find {
                it.value is AndroidTorrentCacheLocation.ExternalPrivate && !it.enabled
            },
        )
    }

    @Test
    fun `test media cache settings is external private`() = runComposeStateTest {
        val context = createMockContext(externalAvailable = true)
        val permissionManager = createTestPermissionManager(null)
        val mediaCacheSettings by createMockMediaCacheSettingsDelegation(
            File(EXTERNAL_PRIVATE_BASE, DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath,
        )
        assertTrue { mediaCacheSettings.value.saveDir?.startsWith(EXTERNAL_PRIVATE_BASE) == true }

        val vm = AndroidTorrentCacheViewModel(context, mediaCacheSettings, permissionManager)
        vm.refreshStorageState()
        takeSnapshot()

        assertEquals(
            AndroidTorrentCacheLocation.ExternalPrivate(
                File(EXTERNAL_PRIVATE_BASE, DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath,
            ),
            vm.currentSelection,
        )

        val locationPresentation = vm.torrentLocationPresentation
        assertEquals(3, locationPresentation.size)
        assertNotNull(locationPresentation.find { it.value is AndroidTorrentCacheLocation.InternalPrivate })
        assertNotNull(
            locationPresentation.find {
                it.value is AndroidTorrentCacheLocation.ExternalPrivate && it.enabled
            },
        )
    }

    @Test
    fun `test media cache settings is external private with external unavailable`() = runComposeStateTest {
        val context = createMockContext(externalAvailable = false)
        val permissionManager = createTestPermissionManager(null)
        val mediaCacheSettings by createMockMediaCacheSettingsDelegation(
            File(EXTERNAL_PRIVATE_BASE, DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath,
        )
        assertTrue { mediaCacheSettings.value.saveDir?.startsWith(EXTERNAL_PRIVATE_BASE) == true }

        val vm = AndroidTorrentCacheViewModel(context, mediaCacheSettings, permissionManager)
        vm.refreshStorageState()
        takeSnapshot()

        val locationPresentation = vm.torrentLocationPresentation
        assertEquals(3, locationPresentation.size)
        assertNotNull(locationPresentation.find { it.value is AndroidTorrentCacheLocation.InternalPrivate })
        assertNotNull(
            locationPresentation.find {
                it.value is AndroidTorrentCacheLocation.ExternalPrivate && !it.enabled
            },
        )
    }

    @Test
    fun `test unknown media cache settings`() = runComposeStateTest {
        val context = createMockContext(externalAvailable = true)
        val permissionManager = createTestPermissionManager(null)
        val mediaCacheSettings by createMockMediaCacheSettingsDelegation(
            File("/system/app/unavailable_path").absolutePath,
        )

        val vm = AndroidTorrentCacheViewModel(context, mediaCacheSettings, permissionManager)
        vm.refreshStorageState()
        takeSnapshot()

        // 如果当前 external private 不可用，那就可能是 external shared。并且同样不可用
        assertEquals(null, vm.currentSelection)

        val locationPresentation = vm.torrentLocationPresentation
        assertEquals(3, locationPresentation.size)
        assertNotNull(locationPresentation.find { it.value is AndroidTorrentCacheLocation.InternalPrivate })
        assertNotNull(
            locationPresentation.find {
                it.value is AndroidTorrentCacheLocation.ExternalPrivate && it.enabled
            },
        )
    }
}

private inline fun runComposeStateTest(
    crossinline block: suspend TestScope.() -> Unit
) = runTest {
    @OptIn(ExperimentalStdlibApi::class)
    Dispatchers.setMain(currentCoroutineContext()[CoroutineDispatcher]!!)
    block()
}

private fun createMockContext(externalAvailable: Boolean): Context {
    return mock<Context> {
        on { filesDir } doReturn File(INTERNAL_PRIVATE_BASE)
        on { getExternalFilesDir(null) } doReturn if (externalAvailable) File(EXTERNAL_PRIVATE_BASE) else null
    }
}

private fun createMockMediaCacheSettingsDelegation(
    initialSaveDir: String? = File(INTERNAL_PRIVATE_BASE, DEFAULT_TORRENT_CACHE_DIR_NAME).absolutePath
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, MediaCacheSettingsDelegation>> {
    return PropertyDelegateProvider { _, property ->
        var currentValue = MediaCacheSettings(saveDir = initialSaveDir)

        val mocked = mock<MediaCacheSettingsDelegation> {
            on { value }.doAnswer { currentValue }
            on { update(any()) }.thenAnswer {
                currentValue = it.arguments[0] as MediaCacheSettings
                it.arguments[0]
            }
            onBlocking { updateSuspended(any()) }.thenAnswer {
                currentValue = it.arguments[0] as MediaCacheSettings
                it.arguments[0]
            }
        }
        ReadOnlyProperty { _, _ -> mocked }
    }
}

private fun createTestPermissionManager(
    requestedPath: String?,/* = File(EXTERNAL_SHARED_BASE, "Movies/Ani").absolutePath*/
    getAccessiblePath: String? = null,
    hasPermission: Boolean = requestedPath != null,
): PermissionManager {
    return object : PermissionManager {
        var requested = false

        override suspend fun requestNotificationPermission(context: ContextMP): Nothing {
            error("unreachable test")
        }

        override suspend fun requestExternalManageableDocument(context: ContextMP): Path? {
            requested = true
            return requestedPath?.let(::Path)
        }

        override suspend fun getExternalManageableDocumentPermission(context: ContextMP, path: Path): Boolean {
            return if (requested) true else hasPermission
        }

        override suspend fun getAccessibleExternalManageableDocumentPath(context: ContextMP): Path? {
            return (if (requested) requestedPath else getAccessiblePath)?.let(::Path)
        }
    }
}

private suspend fun TestScope.takeSnapshot() {
    yield()
    testScheduler.runCurrent()
    Snapshot.sendApplyNotifications()
}