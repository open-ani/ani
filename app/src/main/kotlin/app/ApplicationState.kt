package me.him188.animationgarden.desktop.app

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.impl.model.mutate
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic
import me.him188.animationgarden.api.tags.Episode
import java.io.File

@Stable
class ApplicationState(
    private val client: AnimationGardenClient,
    workingDir: File,
) {
    @Stable
    val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
        })


    @Stable
    val searchQuery: MutableState<SearchQuery> = mutableStateOf(SearchQuery())

    @Stable
    val topicsFlow: KeyedMutableListFlow<String, Topic> = KeyedMutableListFlowImpl { it.id }

    @Stable
    val session: MutableState<SearchSession> by lazy {
        mutableStateOf(client.startSearchSession(SearchQuery()))
    }

    private val dataFile = workingDir.resolve("data").apply { mkdirs() }.resolve("app.yml")

    @Stable
    val saver: AppDataSaver = AppDataSaver(dataFile).apply {
        reload()
    }

    @Stable
    val data: AppData
        get() = saver.data

    @Stable
    val fetcher: Fetcher =
        Fetcher(CoroutineScope(applicationScope.coroutineContext + SupervisorJob(applicationScope.coroutineContext.job)))

    fun updateSearchFilter(searchQuery: SearchQuery) {
        this.searchQuery.value = searchQuery
        fetcher.hasMorePages.value = true
        fetcher.fetchingState.value = FetchingState.Idle
        topicsFlow.value = listOf()
        session.value = client.startSearchSession(searchQuery)
        launchFetchNextPage(!searchQuery.keywords.isNullOrBlank())
    }

    fun launchFetchNextPage(
        continuous: Boolean,
    ) {
        fetcher.launchFetchNextPage(continuous, session.value, topicsFlow)
    }


    @Composable
    fun isEpisodeWatched(episode: Episode): Boolean {
        val starredAnime by remember {
            data.starredAnime.asFlow()
                .map { list -> list.find { it.searchQuery == searchQuery.value.keywords } }
        }.collectAsState(null)

        return starredAnime?.watchedEpisodes?.contains(episode.raw) == true
    }

    fun onEpisodeDownloaded(episode: Episode) {
        data.starredAnime.mutate { list ->
            list.map { anime ->
                if (anime.searchQuery == searchQuery.value.keywords) {
                    anime.run { copy(watchedEpisodes = watchedEpisodes + episode.raw) }
                } else {
                    anime
                }
            }
        }
    }
}

fun ApplicationState.doSearch(keywords: String?) {
    updateSearchFilter(SearchQuery(keywords?.trim(), null, null))
}
