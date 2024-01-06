/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.android.activity

import android.os.Bundle
import android.view.Window
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import me.him188.animationgarden.android.AnimationGardenApplication
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.app.doSearch
import me.him188.animationgarden.app.app.rememberCurrentStarredAnimeState
import me.him188.animationgarden.app.app.settings.toKtorProxy
import me.him188.animationgarden.app.ui.home.HomePage
import me.him188.animationgarden.datasources.api.DownloadSearchQuery
import me.him188.animationgarden.datasources.api.SearchSession
import me.him188.animationgarden.datasources.api.topic.Episode
import me.him188.animationgarden.datasources.api.topic.Topic
import me.him188.animationgarden.datasources.dmhy.DmhyClient

class MainActivity : BaseComponentActivity() {
    private val starredListActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val app = AnimationGardenApplication.instance.app
            onReturnFromStarredList(result, app)
        }

    private var updateAppliedKeyword: ((String) -> Unit)? = null

    private fun onReturnFromStarredList(
        result: ActivityResult,
        app: ApplicationState,
    ) {
        val searchQuery = result.data?.getStringExtra(StarredListActivity.INTENT_KEYWORD) ?: return
        app.updateSearchQuery(DownloadSearchQuery(keywords = searchQuery))
        val episode =
            result.data?.getStringExtra(StarredListActivity.INTENT_EPISODE)?.let { Episode(it) }
        app.organizedViewState.selectedEpisode.value = episode
        updateAppliedKeyword?.invoke(searchQuery)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            val app = remember {
                AnimationGardenApplication.instance.app
            }

            ObserveSettingsChanges(app)


            MaterialTheme(colorScheme) {
                ImmerseStatusBar(AppTheme.colorScheme.background)

                MainPage(app)
            }
        }
    }


    @Composable
    private fun MainPage(app: ApplicationState) {
        CommonAppScaffold {
            HomePage()
//            AndroidMainPage(app, focus)
        }
    }

    @Composable
    private fun ObserveSettingsChanges(app: ApplicationState) {
        val appSettingsManager = remember {
            AnimationGardenApplication.instance.appSettingsManager
        }
        appSettingsManager.attachAutoSave()

        val currentAppSettings by rememberUpdatedState(newValue = appSettingsManager.value.collectAsState().value)
        LaunchedEffect(currentAppSettings.proxy) {
            // proxy changed, update client
            app.client.value = withContext(Dispatchers.IO) {
                DmhyClient.Factory.create {
                    proxy = currentAppSettings.proxy.toKtorProxy()
                }
            }
        }

    }

    @Preview
    @Composable
    private fun PreviewMainPage() {
        ProvideCompositionLocalsForPreview {
            val app = remember {
                ApplicationState(object : DmhyClient {
                    override fun startSearchSession(filter: DownloadSearchQuery): SearchSession<Topic> {
                        return object : SearchSession<Topic> {
                            //                            override val query: DownloadSearchQuery = SearchQuery(null, null, null, null)
                            override val results: Flow<Topic> = flowOf()
                            override suspend fun nextPageOrNull(): List<Topic>? = null
                        }
                    }
                })
            }
            MaterialTheme {
                AndroidMainPage(app, remember { FocusRequester() })
            }
        }
    }


    @Composable
    private fun AndroidMainPage(
        app: ApplicationState,
        searchTextFieldFocus: FocusRequester,
    ) {
        val currentApp by rememberUpdatedState(app)
        val currentTopics by remember { derivedStateOf { currentApp.topicsFlow.asStateFlow() } }.value.collectAsState()

        val appliedKeywordState = rememberSaveable { mutableStateOf("") }
        var currentAppliedKeyword by appliedKeywordState
        val (keywordsInput, onKeywordsInputChange) = rememberSaveable {
            mutableStateOf(
                currentAppliedKeyword
            )
        }

        val currentStarredAnime by app.rememberCurrentStarredAnimeState()

        key(true) {
            // runs only once
            SideEffect {
                updateAppliedKeyword = {
                    currentAppliedKeyword = it
                    onKeywordsInputChange(it)
                }
            }
        }

        Column(
            Modifier
                .background(color = AppTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            // search
            Row(
                Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)


                fun doSearch() {
                    keyboard?.hide()
                    currentAppliedKeyword = keywordsInput.trim()
                    currentApp.doSearch(currentAppliedKeyword)
                }

//                // keywords(search query) input
//                SearchTextField(
//                    keywordsInput,
//                    onKeywordsInputChange,
//                    Modifier
//                        .focusRequester(searchTextFieldFocus)
//                        .defaultMinSize(minWidth = 96.dp)
//                        .weight(0.8f),
//                    doSearch = { doSearch() }
//                )
//
//                // Resizable button for initializing search
//                AnimatedSearchButton {
//                    doSearch()
//                }
            }

            Row(
                Modifier
                    .fillMaxSize()
            ) {
//                TopicsSearchResult(
//                    app,
//                    currentTopics,
//                    isStarred = currentStarredAnime != null,
//                    onClickProxySettings = {
//                        startActivity(SettingsActivity.getIntent(this@MainActivity))
//                    }
//                )
            }
        }
    }
}
