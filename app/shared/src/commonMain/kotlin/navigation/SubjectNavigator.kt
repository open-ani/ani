package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context

interface SubjectNavigator {
    fun navigateToSubjectDetails(context: Context, subjectId: Int)
    fun navigateToEpisode(context: Context, subjectId: Int, episodeId: Int)
}