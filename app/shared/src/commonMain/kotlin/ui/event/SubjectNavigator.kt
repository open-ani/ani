package me.him188.ani.app.ui.event

import me.him188.ani.app.platform.Context

interface SubjectNavigator {
    fun navigateToSubjectDetails(context: Context, subjectId: String)
}