package me.him188.ani.app.platform

import kotlinx.io.files.Path

interface PermissionManager {
    suspend fun requestNotificationPermission(context: ContextMP): Boolean

    /**
     * Android only. 请求一个完全授予可读写权限的外部共享空间路径
     */
    suspend fun requestExternalManageableDocument(context: ContextMP): Path?

    /**
     * Android only. 获取外部共享空间路径的访问权限
     *
     * @return 同时拥有读写权限时才会返回 true
     */
    suspend fun getExternalManageableDocumentPermission(context: ContextMP, path: Path): Boolean

    /**
     * Android only. 获取一个完全授予读写权限的的外部共享空间路径
     */
    suspend fun getAccessibleExternalManageableDocumentPath(context: ContextMP): Path?
}

object GrantedPermissionManager : PermissionManager {
    override suspend fun requestNotificationPermission(context: ContextMP): Boolean {
        return true
    }

    override suspend fun requestExternalManageableDocument(context: ContextMP): Path? {
        return null
    }

    override suspend fun getExternalManageableDocumentPermission(context: ContextMP, path: Path): Boolean {
        return true
    }

    override suspend fun getAccessibleExternalManageableDocumentPath(context: ContextMP): Path? {
        return null
    }
}
