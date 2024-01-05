package me.him188.animationgarden.android

import me.him188.animationgarden.android.ui.event.AndroidSubjectNavigator
import me.him188.animationgarden.app.ui.event.SubjectNavigator
import org.koin.dsl.module

val AndroidModules = module {
    single<SubjectNavigator> { AndroidSubjectNavigator() }
}