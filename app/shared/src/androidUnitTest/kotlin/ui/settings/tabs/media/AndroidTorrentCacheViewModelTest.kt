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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private typealias MediaCacheSettingsDelegation =
        AbstractSettingsViewModel.Settings<MediaCacheSettings, MediaCacheSettings>

private val INTERNAL_PRIVATE_BASE by lazy { File("/data/data/me.him188.ani/files").absolutePath }
private val EXTERNAL_PRIVATE_BASE by lazy {
    File("/storage/emulated/0/Android/data/me.him188.ani/files").absolutePath
}
private val EXTERNAL_SHARED_BASE by lazy { File("/storage/emulated/0/").absolutePath }

class AndroidTorrentCacheViewModelTest : KoinTest {

    @Test
    fun `test default storage location`() = runTest(
        {
            single<PermissionManager> { createTestPermissionManager() }
        },
    ) {
        val context = createMockContext()
        val mediaCacheSettings by createMockMediaCacheSettingsDelegation()
        assertTrue { mediaCacheSettings.value.saveDir?.startsWith(INTERNAL_PRIVATE_BASE) == true }

        val vm = AndroidTorrentCacheViewModel(context, mediaCacheSettings, MockAndroidEnvironment)
        vm.refreshStorageState()
        takeSnapshot()

        assertEquals(
            AndroidTorrentCacheLocation.InternalPrivate(
                File("$INTERNAL_PRIVATE_BASE/$DEFAULT_TORRENT_CACHE_DIR_NAME").absolutePath,
            ),
            vm.currentSelection,
        )
    }

    private fun createMockContext(): Context {
        return mock<Context> {
            on { filesDir } doReturn File(INTERNAL_PRIVATE_BASE)
            on { getExternalFilesDir(null) } doReturn File(EXTERNAL_PRIVATE_BASE)
        }
    }

    private fun createMockMediaCacheSettingsDelegation(
        initialSaveDir: String? = File("$INTERNAL_PRIVATE_BASE/$DEFAULT_TORRENT_CACHE_DIR_NAME").absolutePath
    ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, MediaCacheSettingsDelegation>> {
        return PropertyDelegateProvider { _, property ->
            var currentValue = MediaCacheSettings(saveDir = initialSaveDir)
            val mocked = mock<MediaCacheSettingsDelegation> {
                on { value } doReturn currentValue
                on { update(any()) }.thenAnswer {
                    currentValue = it.arguments[0] as MediaCacheSettings
                    it.arguments[0]
                }
            }
            ReadOnlyProperty { _, _ -> mocked }
        }
    }

    private fun createTestPermissionManager(
        requestedPath: String? = File("$EXTERNAL_SHARED_BASE/Movies/Ani").absolutePath,
        getAccessiblePath: String? = requestedPath,
        hasPermission: Boolean = true
    ): PermissionManager {
        return object : PermissionManager {
            override suspend fun requestNotificationPermission(context: ContextMP): Nothing {
                error("unreachable test")
            }

            override suspend fun requestExternalManageableDocument(context: ContextMP): Path? {
                return requestedPath?.let(::Path)
            }

            override suspend fun getExternalManageableDocumentPermission(context: ContextMP, path: Path): Boolean {
                return hasPermission
            }

            override suspend fun getAccessibleExternalManageableDocumentPath(context: ContextMP): Path? {
                return getAccessiblePath?.let(::Path)
            }
        }
    }

    private inline fun runTest(
        noinline module: ModuleDeclaration,
        crossinline block: suspend TestScope.() -> Unit
    ) = runTest {
        @OptIn(ExperimentalStdlibApi::class)
        Dispatchers.setMain(currentCoroutineContext()[CoroutineDispatcher]!!)

        startKoin { modules(module(moduleDeclaration = module)) }
        block()
        stopKoin()
    }

    private suspend fun TestScope.takeSnapshot() {
        yield()
        testScheduler.runCurrent()
        Snapshot.sendApplyNotifications()
    }
}

private object MockAndroidEnvironment : AndroidEnvironment {
    override fun getExternalStorageDirectory(): File {
        return File(EXTERNAL_SHARED_BASE)
    }
}