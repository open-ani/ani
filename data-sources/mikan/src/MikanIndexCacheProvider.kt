package me.him188.ani.datasources.mikan

interface MikanIndexCacheProvider {
    suspend fun getMikanSubjectId(bangumiSubjectId: String): String?

    // Should not throw
    suspend fun setMikanSubjectId(bangumiSubjectId: String, mikanSubjectId: String)
}

class MemoryMikanIndexCacheProvider : MikanIndexCacheProvider {
    private val cache = mutableMapOf<String, String>()

    override suspend fun getMikanSubjectId(bangumiSubjectId: String): String? {
        return cache[bangumiSubjectId]
    }

    override suspend fun setMikanSubjectId(bangumiSubjectId: String, mikanSubjectId: String) {
        cache[bangumiSubjectId] = mikanSubjectId
    }
}