package me.him188.ani.app.platform

interface PermissionManager {
    suspend fun requestNotificationPermission(context: ContextMP): Boolean
}

object GrantedPermissionManager : PermissionManager {
    override suspend fun requestNotificationPermission(context: ContextMP): Boolean {
        return true
    }
}
