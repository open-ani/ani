package me.him188.ani.android.activity

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.him188.ani.app.activity.BaseComponentActivity
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.subject.details.SubjectDetails
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import org.koin.core.component.KoinComponent

class SubjectDetailsActivity : BaseComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val subjectId = intent.getIntExtra("subjectId", 0).takeIf { it != 0 } ?: run {
            finish()
            return
        }
        val vm = SubjectDetailsViewModel(subjectId)

        enableDrawingToSystemBars()

        setContent {
            AniApp(currentColorScheme) {
//                val windowSizeClass = calculateWindowSizeClass(this)
//                val displayFeatures = calculateDisplayFeatures(this)
                Column(
                    Modifier
                        .fillMaxSize()
                ) {
                    SubjectDetails(vm, goBack = { finish() })
                }
            }
        }
    }

    companion object {
        fun getIntent(context: android.content.Context, subjectId: Int): android.content.Intent {
            return android.content.Intent(context, SubjectDetailsActivity::class.java).apply {
                putExtra("subjectId", subjectId)
            }
        }
    }
}
