package me.him188.ani.android.navigation

import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.android.activity.AuthorizationActivity
import me.him188.ani.app.navigation.AuthorizationNavigator
import me.him188.ani.app.navigation.AuthorizationNavigator.AuthorizationResult
import me.him188.ani.app.platform.Context
import me.him188.ani.app.session.SessionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidAuthorizationNavigator : AuthorizationNavigator, DefaultLifecycleObserver, KoinComponent {
    private val sessionManager: SessionManager by inject()

    override suspend fun authorize(context: Context, optional: Boolean): AuthorizationResult = mutex.withLock {
        if (sessionManager.isSessionValid.value) {
            return@withLock AuthorizationResult.SUCCESS
        }
        check(currentResult == null) { "currentResult should be null" }
        currentResult = CompletableDeferred()
        context.startActivity(AuthorizationActivity.getIntent(context, optional))
        val res = currentResult!!.await()
        currentResult = null
        return res
    }

    companion object {
        private val mutex = Mutex()
        var currentResult: CompletableDeferred<AuthorizationResult>? = null
            private set
    }
}