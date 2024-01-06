package me.him188.animationgarden.android.activity

import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import me.him188.animationgarden.app.ui.subject.details.SubjectDetails
import me.him188.animationgarden.app.ui.subject.details.SubjectDetailsViewModel
import org.koin.core.component.KoinComponent

class SubjectDetailsActivity : BaseComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val subjectId = intent.getStringExtra("subjectId") ?: run {
            finish()
            return
        }
        val vm = SubjectDetailsViewModel(subjectId)

        enableEdgeToEdge(
            SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme(currentColorScheme) {
//                val windowSizeClass = calculateWindowSizeClass(this)
//                val displayFeatures = calculateDisplayFeatures(this)
                Column(
                    Modifier
                        .fillMaxSize()
                ) {
                    SubjectDetails(vm)
                }
            }
        }
    }

    companion object {
        fun getIntent(context: android.content.Context, subjectId: String): android.content.Intent {
            return android.content.Intent(context, SubjectDetailsActivity::class.java).apply {
                putExtra("subjectId", subjectId)
            }
        }
    }
}
