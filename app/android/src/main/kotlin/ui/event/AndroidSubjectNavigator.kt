package me.him188.ani.android.ui.event

import me.him188.ani.android.activity.SubjectDetailsActivity
import me.him188.ani.app.platform.Context
import me.him188.ani.app.ui.event.SubjectNavigator

class AndroidSubjectNavigator : SubjectNavigator {
    override fun navigateToSubjectDetails(context: Context, subjectId: String) {
        context.startActivity(SubjectDetailsActivity.getIntent(context, subjectId))
    }
}
