package me.him188.animationgarden.app.ui.subject.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen

class SubjectDetailsScreen(
    private val subjectId: String,
) : Screen {
    @Composable
    override fun Content() {
        val vm = remember(subjectId) { SubjectDetailsViewModel(subjectId) }
        SubjectDetails(vm)
    }
}
