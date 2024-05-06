package me.him188.ani.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun CoroutineContext.childScopeContext(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineContext {
    check(coroutineContext[Job] == null) { "Additional coroutineContext cannot have a Job" }
    val job = this[Job] ?: error("No Job in the context")
    return this + SupervisorJob(job) + coroutineContext
}

fun CoroutineContext.childScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    return CoroutineScope(childScopeContext(coroutineContext))
}

fun CoroutineScope.childScopeContext(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineContext =
    this.coroutineContext.childScopeContext(coroutineContext)

fun CoroutineScope.childScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope =
    this.coroutineContext.childScope(coroutineContext) 
