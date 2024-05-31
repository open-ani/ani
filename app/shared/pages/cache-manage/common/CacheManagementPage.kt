package me.him188.ani.app.pages.cache.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.ui.feedback.ErrorDialogHost
import me.him188.ani.app.ui.feedback.ErrorMessage
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.datasources.core.cache.MediaStats
import me.him188.ani.datasources.core.cache.emptyMediaStats
import me.him188.ani.datasources.core.cache.sum
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
interface CacheManagementPageViewModel {
    val overallStats: MediaStats
    val storages: List<MediaCacheStorageState>?
    val accumulatedList: List<MediaCachePresentation>?

    val errorMessage: StateFlow<ErrorMessage?>
    fun delete(item: MediaCachePresentation)
}

@Stable
class CacheManagementPageViewModelImpl : CacheManagementPageViewModel,
    AbstractViewModel(),
    KoinComponent {

    private val cacheManager: MediaCacheManager by inject()
    private val storagesFlow = cacheManager.enabledStorages.map { list ->
        list.map { storage ->
            MediaCacheStorageState(storage)
        }
    }
    override val overallStats: MediaStats by cacheManager.enabledStorages.map { list ->
        list.map { it.stats }.sum()
    }.produceState(emptyMediaStats())

    override val storages by storagesFlow.produceState(emptyList())

    private val items = mutableMapOf<MediaCache, MediaCachePresentation>()

    override val accumulatedList: List<MediaCachePresentation>? by kotlin.run {
        val mediaCacheListFromStorages = cacheManager.storages.map { storageFlow ->
            storageFlow.flatMapLatest { storage ->
                if (storage == null) {
                    return@flatMapLatest emptyFlow()
                }
                storage.listFlow
            }.onStart { emit(emptyList()) }
        }

        combine(mediaCacheListFromStorages) { lists ->
            lists.asSequence()
                .flatten()
                .toList()
                .let {
                    mapCacheToItem(it)
                }
        }.produceState(null)
    }
    override val errorMessage = MutableStateFlow<ErrorMessage?>(null)

    private fun mapCacheToItem(list: List<MediaCache>): List<MediaCachePresentation> {
        return list.map { cache ->
            items.getOrPut(cache) {
                MediaCachePresentation(cache)
            }
        }.also {
            items.keys.removeAll { key -> key !in list }
        }
    }

    override fun delete(item: MediaCachePresentation) {
        if (errorMessage.value != null) return
        val job = backgroundScope.launch(start = CoroutineStart.LAZY) {
            try {
                storages.forEach { storage ->
                    storage.storage.delete(item.cache)
                }
                items.remove(item.cache)
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = ErrorMessage.simple("删除缓存失败", e)
            }
        }
        errorMessage.value = ErrorMessage.processing("正在删除缓存", onCancel = { job.cancel() })
        job.start()
    }
}

@Stable
class MediaCacheStorageState(
    val storage: MediaCacheStorage,
) : KoinComponent {
    val mediaSourceId = storage.mediaSourceId
}

@Composable
fun CacheManagementPage(
    modifier: Modifier = Modifier,
    vm: CacheManagementPageViewModel = rememberViewModel { CacheManagementPageViewModelImpl() },
    showBack: Boolean = !isShowLandscapeUI(),
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text("缓存管理") },
                navigationIcon = {
                    if (showBack) {
                        TopAppBarGoBackButton()
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val state = rememberLazyGridState()

            StorageOverallStats(
                vm.overallStats,
                Modifier.fillMaxWidth()
                    .then(if (state.canScrollBackward) Modifier.shadow(2.dp, clip = false) else Modifier)
            )

            val storages = vm.storages
            if (storages?.isEmpty() == true) {
                Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("未启用任何缓存服务, 请在设置中至少启用一个", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                val list = vm.accumulatedList
                if (list?.isEmpty() == true) {
                    Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("还未缓存任何内容", style = MaterialTheme.typography.titleMedium)
                    }
                }
                ErrorDialogHost(vm.errorMessage)
                StorageManagerView(
                    list ?: emptyList(),
                    onDelete = { item ->
                        vm.delete(item)
                    },
                    Modifier.padding(horizontal = 16.dp).padding(top = 2.dp).fillMaxWidth(),
                    state = state,
                )
            }
        }
    }
}

@Composable
fun StorageOverallStats(
    stats: MediaStats,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        Column(
            Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Stat(
                title = {
                    Icon(Icons.Rounded.Upload, null)
                    Text("总上传", style = MaterialTheme.typography.titleMedium)
                },
                speedText = {
                    val speed by stats.uploadRate.collectAsStateWithLifecycle(FileSize.Unspecified)
                    Text(renderSpeed(speed))
                },
                totalText = {
                    val speed by stats.uploaded.collectAsStateWithLifecycle(FileSize.Unspecified)
                    Text(renderFileSize(speed))
                }
            )

            Stat(
                title = {
                    Icon(Icons.Rounded.Download, null)
                    Text("总下载", style = MaterialTheme.typography.titleMedium)
                },
                speedText = {
                    val speed by stats.downloadRate.collectAsStateWithLifecycle(FileSize.Unspecified)
                    Text(renderSpeed(speed))
                },
                totalText = {
                    val speed by stats.downloaded.collectAsStateWithLifecycle(FileSize.Unspecified)
                    Text(renderFileSize(speed))
                },
            )
        }
    }
}

@Composable
private fun Stat(
    title: @Composable () -> Unit,
    speedText: @Composable () -> Unit,
    totalText: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            title()
        }

        Row(
            Modifier.weight(1f).padding(start = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Center)) {
                Row(
                    Modifier.widthIn(min = 100.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Speed, null)
                    speedText()
                }
                Row(
                    Modifier.widthIn(min = 100.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.DownloadDone, null)
                    totalText()
                }
            }
        }
    }
}

// Management for a single storage
@Composable
fun StorageManagerView(
    list: List<MediaCachePresentation>,
    onDelete: (MediaCachePresentation) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
) {
    LazyVerticalGrid(
        GridCells.Adaptive(360.dp),
        modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { }

        items(list, key = { it.cache.cacheId }) { item ->
            CacheItemView(item, onDelete, { item.mediaSourceId })
        }

        item(span = { GridItemSpan(maxLineSpan) }) { }
    }
}
