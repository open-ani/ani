package me.him188.ani.app.platform

import android.Manifest
import android.os.Build
import kotlinx.io.files.Path
import me.him188.ani.utils.logging.logger

class AndroidPermissionManager : PermissionManager {
    private val logger = logger<AndroidPermissionManager>()
    override suspend fun requestNotificationPermission(context: ContextMP): Boolean {
        // To send notifications on API levels below 32, you don't need to request the POST_NOTIFICATIONS permission,
        // as it is only required for API level 33 (Android 13) and above.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return requestPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    }

    private suspend fun requestPermission(context: ContextMP, permission: String): Boolean {
        val activity = context.findActivity() as? BaseComponentActivity ?: return false
        return activity.requestPermission(permission)
    }

    override suspend fun requestExternalManageableDocument(context: ContextMP): Path? {
        val activity = context.findActivity() as? BaseComponentActivity ?: return null
        val result = activity.requestExternalManageableDocument()
        logger.info("request external shared directory result: $result")
        return result
    }

    override suspend fun getExternalManageableDocumentPermission(context: ContextMP, path: Path): Boolean {
        val activity = context.findActivity() as? BaseComponentActivity ?: return false
        return activity.getExternalManageableDocumentPermission(path)
    }

    override suspend fun getAccessibleExternalManageableDocumentPath(context: ContextMP): Path? {
        val activity = context.findActivity() as? BaseComponentActivity ?: return null
        return activity.getAccessibleExternalManageableDocumentPath()
    }
}
