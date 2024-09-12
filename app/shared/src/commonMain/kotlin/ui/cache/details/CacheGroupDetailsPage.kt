package me.him188.ani.app.ui.cache.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class MediaCacheDetailsPageViewModel(
    private val cacheId: String,
) : AbstractViewModel(), KoinComponent {
    private val cacheManager: MediaCacheManager by inject()
    private val mediaSourceManager: MediaSourceManager by inject()

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
    val sourceInfo: MediaSourceInfo? by originMediaFlow.flatMapLatest { media ->
        media?.mediaSourceId?.let { mediaSourceManager.infoFlowByMediaSourceId(it) } ?: flowOf(null)
    }.produceState(null)
}

@Composable
fun MediaCacheDetailsPage(
    vm: MediaCacheDetailsPageViewModel,
    modifier: Modifier = Modifier,
    allowBack: Boolean = true,
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    MediaCacheDetailsPage(
        media = vm.media,
        sourceInfo = vm.sourceInfo,
        modifier = modifier,
        allowBack = allowBack,
        windowInsets = windowInsets,
    )
}

@Composable
fun MediaCacheDetailsPage(
    media: Media?,
    sourceInfo: MediaSourceInfo?,
    modifier: Modifier = Modifier,
    allowBack: Boolean = true,
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
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
                colors = AniThemeDefaults.topAppBarColors(),
                windowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
        },
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
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
                            Surface(
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                                    .padding(vertical = 16.dp),
                                color = ListItemDefaults.containerColor, // fill gap between items
                            ) {
                                MediaDetailsColumn(
                                    it,
                                    sourceInfo = sourceInfo,
                                    Modifier.fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
