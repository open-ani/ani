/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.him188.ani.app.domain.torrent.IRemoteAniTorrentEngine
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class TorrentServiceConnection(
    private val context: Context
): LifecycleEventObserver, ServiceConnection {
    private val logger = logger<TorrentServiceConnection>()
    
    private val binder: AtomicReference<IRemoteAniTorrentEngine> = AtomicReference()
    val connected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    
    private val awaitMap = ConcurrentHashMap<Any, CompletableDeferred<IRemoteAniTorrentEngine>>()
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                val bindResult = context.bindService(
                    Intent(context, AniTorrentService::class.java), this, Context.BIND_ABOVE_CLIENT
                )
                if (!bindResult) logger.error { "Failed to bind AniTorrentService." }
            }

            Lifecycle.Event.ON_DESTROY -> {
                try {
                    context.unbindService(this)
                } catch (ex: IllegalArgumentException) {
                    logger.warn { "Failed to unregister AniTorrentService service." }
                }
            }

            else -> {}
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        logger.debug { "AniTorrentService is connected, name = $name" }
        if (service != null) {
            val result = IRemoteAniTorrentEngine.Stub.asInterface(service)
            
            binder.compareAndSet(null, result)
            connected.value = true
            
            awaitMap.forEach { (_, u) -> u.complete(result) }
            awaitMap.clear()
        } else {
            logger.error { "Failed to get binder of AniTorrentService." }
            connected.value = false
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        logger.debug { "AniTorrentService is disconnected, name = $name" }
        binder.set(null)
        connected.value = false
    }

    suspend fun awaitBinder(): IRemoteAniTorrentEngine {
        val currentBinder = binder.get()
        if (currentBinder != null) return currentBinder
        
        val key = Any()
        val deferred = CompletableDeferred<IRemoteAniTorrentEngine>()
        
        awaitMap[key] = deferred
        return withContext(CoroutineExceptionHandler { _, _ -> awaitMap.remove(key) }) {
            deferred.await()
        }
    }
}