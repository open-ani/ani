package me.him188.animationgarden.app.ui.event

import me.him188.animationgarden.app.platform.Context

interface SubjectNavigator {
    fun navigateToSubjectDetails(context: Context, subjectId: String)
}