/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.platform

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


@SuppressLint("MissingPermission")
private class AndroidMeteredNetworkDetector(
    context: Context
) : MeteredNetworkDetector {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val flow = MutableStateFlow(getCurrentIsMetered())
    override val isMeteredNetworkFlow: Flow<Boolean> get() = flow

    // Create a NetworkCallback to detect network changes
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { // 连接 WiFi
            flow.tryEmit(getCurrentIsMetered())
        }

        override fun onLost(network: Network) { // 断开 WiFi
            flow.tryEmit(getCurrentIsMetered())
        }

        // WiFi 设置变更 (设置为计费网络)
        // 连接/断开 WiFi 不会触发 onCapabilitiesChanged
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val isMetered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            flow.tryEmit(isMetered)
        }
    }

    init {
        // Register the NetworkCallback instead of using BroadcastReceiver
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Emit the first value
        flow.tryEmit(getCurrentIsMetered())
    }

    private fun getCurrentIsMetered(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        // Return whether the network is metered or not
        val isMetered = !activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        return isMetered
    }

    override fun dispose() {
        // Unregister the network callback when no longer needed
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

actual fun createMeteredNetworkDetector(context: Context): MeteredNetworkDetector {
    return AndroidMeteredNetworkDetector(context)
}