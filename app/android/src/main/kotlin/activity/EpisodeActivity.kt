package me.him188.ani.android.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.him188.ani.app.activity.BaseComponentActivity
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.subject.episode.EpisodePage
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel

class EpisodeActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val episodeId = intent.getIntExtra("episodeId", 0).takeIf { it != 0 } ?: run {
            finish()
            return
        }
        val subjectId = intent.getIntExtra("subjectId", 0).takeIf { it != 0 } ?: run {
            finish()
            return
        }
        enableDrawingToSystemBars()

        val vm = EpisodeViewModel(subjectId, episodeId)
        setContent {
            AniApp(currentColorScheme) {
                Column(Modifier.fillMaxSize()) {
                    EpisodePage(vm, goBack = { finish() })
                }
            }
        }
    }

    companion object {
        fun getIntent(context: android.content.Context, subjectId: Int, episodeId: Int): android.content.Intent {
            return android.content.Intent(context, EpisodeActivity::class.java).apply {
                putExtra("subjectId", subjectId)
                putExtra("episodeId", episodeId)
            }
        }
    }
}