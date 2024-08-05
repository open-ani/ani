/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.platform

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Stable
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import me.him188.ani.app.tools.DocumentsContractApi19
import me.him188.ani.utils.io.toFile
import java.util.concurrent.ConcurrentLinkedQueue


abstract class BaseComponentActivity : ComponentActivity() {
    @Stable
    val snackbarHostState = SnackbarHostState()

    private val requestPermissionHandlers: MutableCollection<(Boolean) -> Unit> = ConcurrentLinkedQueue()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            requestPermissionHandlers.forEach { it.invoke(granted) }
        }
    private val requestPermissionLock = Mutex()

    private val requestExternalManageableDocumentHandler: AtomicRef<((Uri?) -> Unit)?> = atomic(null)
    private val requestExternalManageableDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) applicationContext.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            val handler by requestExternalManageableDocumentHandler
            handler?.invoke(uri)
        }

    suspend fun requestPermission(permission: String): Boolean {
        val res = CompletableDeferred<Boolean>()
        return requestPermissionLock.withLock {
            val handler: (Boolean) -> Unit = { res.complete(it) }
            requestPermissionHandlers.add(handler)
            try {
                requestPermissionLauncher.launch(permission)
                res.await()
            } finally {
                requestPermissionHandlers.remove(handler)
            }
        }
    }

    suspend fun requestExternalManageableDocument(): Path? {
        val res = CompletableDeferred<Path?>()
        val handler: (Uri?) -> Unit = { uri: Uri? ->
            res.complete(uri?.let { DocumentsContractApi19.parseUriToStorage(this, it)?.let(::Path) })
        }

        if (!requestExternalManageableDocumentHandler.compareAndSet(null, handler)) {
            return null
        }

        return try {
            requestExternalManageableDocumentLauncher.launch(null)
            res.await()
        } finally {
            requestExternalManageableDocumentHandler.compareAndSet(handler, null)
        }
    }

    fun getExternalManageableDocumentPermission(path: Path): Boolean {
        val file = path.toFile()
        var grant = false
        applicationContext.contentResolver.persistedUriPermissions.run b@{
            forEach { p ->
                val storage = DocumentsContractApi19.parseUriToStorage(this@BaseComponentActivity, p.uri)
                if (storage != null && file.startsWith(storage) && p.isReadPermission && p.isWritePermission) {
                    grant = true
                    return@b
                }
            }
        }
        return grant
    }

    /**
     * 获取一个完全授予读写权限的的外部共享空间路径，对于不可用的路径，将会释放持久化权限
     */
    fun getAccessibleExternalManageableDocumentPath(): Path? {
        val externalPrivateBasePath = getExternalFilesDir(null)?.absolutePath
        val externalStorageBasePath = Environment.getExternalStorageDirectory().absolutePath
        var result: Path? = null

        applicationContext.contentResolver.persistedUriPermissions.forEach { p ->
            val path = DocumentsContractApi19.parseUriToStorage(this, p.uri) ?: return@forEach

            if (externalPrivateBasePath != null && path.startsWith(externalPrivateBasePath)) {
                // 不可能出现的情况，external private directory 永远对 App 可用
                // 所以不会出现在 persistedUriPermissions 中
                return@forEach
            }

            if (!path.startsWith(externalStorageBasePath)) {
                // 不是外部共享空间的 uri，不处理
                return@forEach
            }

            if (!p.isReadPermission || !p.isWritePermission) {
                // 没有完整的读写权限，从 persistedUriPermissions 中移除，因为我们申请 persistedUriPermissions 仅有一种用途，
                // 所以其他的 persistedUriPermissions 可以被视为无效路径或者用不到的路径
                applicationContext.contentResolver.releasePersistableUriPermission(
                    p.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
                return@forEach
            }

            result = Path(path)
        }

        return result
    }

    fun enableDrawingToSystemBars() {
        enableEdgeToEdge(
            SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

suspend fun BaseComponentActivity.showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
}

fun BaseComponentActivity.showSnackbarAsync(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    lifecycleScope.launch(Dispatchers.Main) {
        try {
            snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
        } catch (e: Exception) { // exception will crash app
            e.printStackTrace()
        }
    }
}
