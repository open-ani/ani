package me.him188.ani.app.ui.collection

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.him188.ani.app.data.CollectionRepository
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyCollectionsViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val collectionRepository: CollectionRepository by inject()
    private val authorizationNavigator: AuthorizationNavigator by inject()

    val isLoggedIn = sessionManager.isSessionValid.filterNotNull().shareInBackground()

    val collections = sessionManager.username.filterNotNull().flatMapLatest { username ->
        collectionRepository.getCollections(username)
    }.shareInBackground()
}