/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.platform

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow


private class AndroidMeteredNetworkDetector(
    private val context: Context
) : BroadcastReceiver(), MeteredNetworkDetector {
    private val flow = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    override val isMeteredNetworkFlow: Flow<Boolean> = flow
    
    private val connectivityManager by lazy { 
        context.getSystemService(ConnectivityManager::class.java) 
    }
    
    init {
        context.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        // emit first value
        flow.tryEmit(getCurrentIsMetered())
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        flow.tryEmit(getCurrentIsMetered())
    }
    
    private fun getCurrentIsMetered(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return !activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }

    override fun dispose() {
        context.unregisterReceiver(this)
    }
    
}

actual fun createMeteredNetworkDetector(context: Context): MeteredNetworkDetector {
    return AndroidMeteredNetworkDetector(context)
}