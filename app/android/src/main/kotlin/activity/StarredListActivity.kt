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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.android.AnimationGardenApplication
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.app.ApplicationState
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.platform.LocalContext

class StarredListActivity : BaseComponentActivity() {
    companion object {
        const val INTENT_KEYWORD = "me.him188.animationgarden.android.keyword"
        const val INTENT_EPISODE = "me.him188.animationgarden.android.episode"

        fun getIntent(context: Context): Intent = Intent(context, StarredListActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            MaterialTheme(colorScheme) {
                ImmerseStatusBar(AppTheme.colorScheme.primary)

                CommonAppScaffold(
                    topBar = {
                        CommonTopAppBar(
                            navigationIcon = {
                                IconButton(onClick = {
                                    this.setResult(Activity.RESULT_CANCELED)
                                    finish()
                                }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        LocalI18n.current.getString("menu.back")
                                    )
                                }
                            },
                            title = {
                                Text(text = LocalI18n.current.getString("window.starred.list.title"))
                            },
                        )
                    }
                ) {
                    Box(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
                        StarredListPage(app = remember { AnimationGardenApplication.instance.app })
                    }
                }
            }
        }
    }
}

@Composable
private fun StarredListPage(app: ApplicationState) {
    val currentApp by rememberUpdatedState(newValue = app)
    val activity by rememberUpdatedState(newValue = LocalContext.current as? ComponentActivity)

    val currentStarredAnimeList by app.starredAnimeListState

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(currentStarredAnimeList, key = { it.id }) { anime ->
            val currentAnime by rememberUpdatedState(anime)
//            LaunchedEffect(anime.id) {
//                delay(3.seconds) // ignore if user is quickly scrolling
//                currentApp.updateStarredAnimeEpisodesIfNeeded(anime, currentAnime)
//            }


//            StarredAnimeCard(
//                anime = anime,
//                onStarRemove = {
//                    currentApp.launchDataSynchronization {
//                        commit(StarredAnimeMutations.Remove(currentAnime.id))
//                    }
//                },
//                onClick = { episode ->
//                    activity?.setResult(Activity.RESULT_OK, Intent().apply {
//                        putExtra(StarredListActivity.INTENT_KEYWORD, currentAnime.searchQuery)
//                        putExtra(StarredListActivity.INTENT_EPISODE, episode?.raw)
//                    })
//                    activity?.finish()
//                }
//            )
        }
    }
}
