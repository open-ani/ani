package me.him188.ani.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.bangumi.BangumiClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.Subject

interface SubjectRepository {
    suspend fun getSubject(id: Int): Subject?
}

class SubjectRepositoryImpl : SubjectRepository, KoinComponent {
    private val client: BangumiClient by inject()

    override suspend fun getSubject(id: Int): Subject? {
        return withContext(Dispatchers.IO) {
            runCatching {
                client.api.getSubjectById(id)
            }.getOrNull()
        }
    }
}

private class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize + 1, 1f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxSize
    }
}
