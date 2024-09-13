package me.him188.ani.app.platform

import android.Manifest
import android.os.Build
import me.him188.ani.utils.logging.info
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

    override suspend fun requestExternalDocumentTree(context: ContextMP): String? {
        val activity = context.findActivity() as? BaseComponentActivity ?: return null
        val result = activity.requestExternalDocumentTree()
        logger.info { "request external document tree result: $result" }
        return result
    }
}
