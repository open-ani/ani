package me.him188.ani.app.ui.cache.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.datasources.api.Media
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class MediaCacheDetailsPageViewModel(
    private val cacheId: String,
) : AbstractViewModel(), KoinComponent {
    private val cacheManager: MediaCacheManager by inject()

    private val mediaCacheFlow = cacheManager.enabledStorages.flatMapLatest { storages ->
        combine(
            storages.map { storage ->
                storage.listFlow.map { caches ->
                    caches.find { it.cacheId == cacheId }
                }
            },
        ) { results ->
            results.firstNotNullOfOrNull { it }
        }
    }

    private val originMediaFlow = mediaCacheFlow.map { it?.origin }

    val media by originMediaFlow.produceState(null)
}

@Composable
fun MediaCacheDetailsPage(
    vm: MediaCacheDetailsPageViewModel,
    modifier: Modifier = Modifier,
    allowBack: Boolean = true,
) {
    MediaCacheDetailsPage(
        media = vm.media,
        modifier = modifier,
        allowBack = allowBack,
    )
}

@Composable
fun MediaCacheDetailsPage(
    media: Media?,
    modifier: Modifier = Modifier,
    allowBack: Boolean = true,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("详情") },
                navigationIcon = {
                    if (allowBack) {
                        TopAppBarGoBackButton()
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(paddingValues)
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth),
            ) {
                Surface(Modifier.fillMaxHeight()) {
                    AnimatedVisibility(
                        visible = media != null,
                        enter = fadeIn(tween(500)) + slideInVertically(
                            tween(600),
                            initialOffsetY = { 50.coerceAtMost(it) },
                        ),
                        exit = fadeOut(snap()),
                    ) {
                        media?.let {
                            MediaDetailsColumn(
                                it,
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                                    .padding(vertical = 16.dp)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}
