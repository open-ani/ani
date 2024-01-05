package me.him188.animationgarden.android.ui.event

import me.him188.animationgarden.android.activity.SubjectDetailsActivity
import me.him188.animationgarden.app.platform.Context
import me.him188.animationgarden.app.ui.event.SubjectNavigator

class AndroidSubjectNavigator : SubjectNavigator {
    override fun navigateToSubjectDetails(context: Context, subjectId: String) {
        context.startActivity(SubjectDetailsActivity.getIntent(context, subjectId))
    }
}
