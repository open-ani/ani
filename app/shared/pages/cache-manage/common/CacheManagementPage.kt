package me.him188.ani.app.pages.cache.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.episode.mediaFetch.renderMediaSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.Subject

@Stable
interface CacheManagementPageViewModel {
    val storages: List<MediaCacheStorageState>
}

@Stable
class CacheManagementPageViewModelImpl : CacheManagementPageViewModel,
    AbstractViewModel(),
    KoinComponent {

    private val cacheManager: MediaCacheManager by inject()

    override val storages = cacheManager.storages.map { storage ->
        MediaCacheStorageState(storage).also {
            addCloseable(it)
        }
    }
}

@Stable
class MediaCacheStorageState(
    private val storage: MediaCacheStorage
) : AbstractViewModel(), KoinComponent {
    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()

    private val items = mutableMapOf<String, CacheItem>()

    val mediaSourceId = storage.mediaSourceId

    /**
     * A flow that subscribes on all the caches in the storage.
     */
    val list: Flow<List<CacheItem>> = storage.listFlow.flatMapLatest { list ->
        mapCacheToItem(list)
    }

    private fun mapCacheToItem(list: List<MediaCache>): Flow<List<CacheItem>> {
        return list.asFlow().map { cache ->
            items.getOrPut(cache.origin.mediaId) {
                val metadata = cache.metadata
                CacheItem(
                    cache,
                    null,
//                  subject =  metadata.subjectId?.toIntOrNull()?.let {
//                        runCatching {
//                            subjectRepository.getSubject(it)
//                        }.getOrNull()
//                    },
//                   episode = metadata.episodeId?.toIntOrNull()?.let { episodeRepository.getEpisodeById(it) },
                )
            }
        }.also {
            val ids = list.map { it.origin.mediaId }
            items.keys.removeAll { key -> key !in ids }
        }.runningList()
    }

    fun delete(item: CacheItem) {
        items.remove(item.origin.mediaId)
        launchInBackground {
            storage.delete(item.origin)
        }
    }
}

@Stable
class CacheItem(
    val cache: MediaCache,
    subject: Subject?,
//    private val episode: EpisodeDetail?,
//    val episode: Flow<Episode>,
) {
    val origin: Media get() = cache.origin

    //    val subjectImage = subject?.images?.large
    val subjectName = subject?.nameCNOrName() ?: cache.metadata.subjectNames.firstOrNull() ?: "未知"
    val episodeName = cache.metadata.episodeName

    val mediaSourceId = cache.origin.mediaId
    val episodeSort = cache.metadata.episodeSort

    val progress = cache.progress
    val totalSize = cache.totalSize
}

@Composable
fun CacheManagementPage(
    modifier: Modifier = Modifier,
    vm: CacheManagementPageViewModel = rememberViewModel { CacheManagementPageViewModelImpl() },
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text("缓存管理") },
                navigationIcon = {
                    TopAppBarGoBackButton()
                },
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val storages = vm.storages

            if (storages.isEmpty()) {
                Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("未添加任何缓存服务", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                for (storage in storages) {
                    val list by storage.list.collectAsStateWithLifecycle(null)
                    if (list?.isEmpty() == true) {
                        Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("还未缓存任何内容", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    StorageManagerView(
                        list ?: emptyList(),
                        storage.mediaSourceId,
                        onDelete = {
                            storage.delete(it)
                        },
                        Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// Management for a single storage
@Composable
fun StorageManagerView(
    list: List<CacheItem>,
    mediaSourceId: String,
    onDelete: (CacheItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { }

        items(list, key = { it.origin.mediaId }) { item ->
            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        renderMediaSource(item.subjectName),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )

                    IconButton(
                        { onDelete(item) },
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = "删除")
                    }
                }

                Column(
                    Modifier.padding(bottom = 16.dp).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row {
                        Text(
                            renderMediaSource(item.episodeSort.toString()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            renderMediaSource(item.episodeName),
                            Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        // progress bar
                        val progress by item.progress.collectAsStateWithLifecycle(null)
                        if (progress != null && progress != 1f) {
                            Row {
                                LinearProgressIndicator(
                                    progress = { progress ?: 0f },
                                    Modifier.weight(1f),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (progress == 1f) {
                                Icon(
                                    Icons.Rounded.DownloadDone,
                                    null,
                                    Modifier.padding(end = 8.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Downloading,
                                    null,
                                    Modifier.padding(end = 8.dp)
                                )
                            }

                            Text(renderMediaSource(mediaSourceId))

                            val totalSize by item.totalSize.collectAsStateWithLifecycle(null)
                            Text(
                                remember(totalSize) {
                                    totalSize?.toString().orEmpty()
                                },
                                Modifier.padding(start = 16.dp),
                            )
                        }
                    }
                }
            }
        }

        item { }
    }
}
