package me.him188.ani.android.navigation

import me.him188.ani.android.activity.EpisodeActivity
import me.him188.ani.android.activity.SubjectDetailsActivity
import me.him188.ani.app.navigation.SubjectNavigator
import me.him188.ani.app.platform.Context

class AndroidSubjectNavigator : SubjectNavigator {
    override fun navigateToSubjectDetails(context: Context, subjectId: Int) {
        context.startActivity(SubjectDetailsActivity.getIntent(context, subjectId))
    }

    override fun navigateToEpisode(context: Context, subjectId: Int, episodeId: Int) {
        context.startActivity(EpisodeActivity.getIntent(context, subjectId, episodeId))
    }
}
