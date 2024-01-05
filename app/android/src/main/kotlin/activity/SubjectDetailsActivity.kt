package me.him188.animationgarden.android.activity

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.ui.subject.SubjectDetails
import me.him188.animationgarden.app.ui.subject.SubjectDetailsViewModel
import org.koin.core.component.KoinComponent

class SubjectDetailsActivity : BaseComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val subjectId = intent.getStringExtra("subjectId") ?: run {
            finish()
            return
        }
        val vm = SubjectDetailsViewModel(subjectId)

        setContent {
            MaterialTheme(colorScheme) {
                ImmerseStatusBar(AppTheme.colorScheme.primary)

                SubjectDetails(vm)
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
