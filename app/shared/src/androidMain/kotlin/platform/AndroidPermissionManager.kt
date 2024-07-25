package me.him188.ani.app.platform

import android.Manifest
import android.os.Build

class AndroidPermissionManager : PermissionManager {
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
}
