package me.him188.ani.android

import me.him188.ani.android.navigation.AndroidAuthorizationNavigator
import me.him188.ani.android.navigation.AndroidSubjectNavigator
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.navigation.SubjectNavigator
import org.koin.dsl.module

fun getAndroidModules() = module {
    single<SubjectNavigator> { AndroidSubjectNavigator() }
    single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
}