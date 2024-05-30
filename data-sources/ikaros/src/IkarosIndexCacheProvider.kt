package me.him188.ani.datasources.ikaros

interface IkarosIndexCacheProvider {
    suspend fun getIkarosSubjectId(bangumiSubjectId: String): String?

    // Should not throw
    suspend fun setIkarosSubjectId(bangumiSubjectId: String, ikarosSubjectId: String)
}

class MemoryIkarosIndexCacheProvider : IkarosIndexCacheProvider {
    private val cache = mutableMapOf<String, String>()

    override suspend fun getIkarosSubjectId(ikarosSubjectId: String): String? {
        return cache[ikarosSubjectId]
    }

    override suspend fun setIkarosSubjectId(bangumiSubjectId: String, ikarosSubjectId: String) {
        cache[bangumiSubjectId] = ikarosSubjectId
    }
}