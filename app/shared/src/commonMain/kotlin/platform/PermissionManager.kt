package me.him188.ani.app.platform

interface PermissionManager {
    suspend fun requestNotificationPermission(context: ContextMP): Boolean

    /**
     * Android only. 请求一个完全授予可读写权限的外部共享空间路径，返回其 URL string
     */
    suspend fun requestExternalDocumentTree(context: ContextMP): String?
}

object GrantedPermissionManager : PermissionManager {
    override suspend fun requestNotificationPermission(context: ContextMP): Boolean {
        return true
    }

    override suspend fun requestExternalDocumentTree(context: ContextMP): String? {
        return null
    }
}
