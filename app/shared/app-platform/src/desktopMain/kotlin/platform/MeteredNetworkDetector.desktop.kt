/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.platform

import com.sun.jna.platform.win32.COM.COMException
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WTypes
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.currentPlatformDesktop


private class WindowsMeteredNetworkDetector : MeteredNetworkDetector {
    private val logger = logger<MeteredNetworkDetector>()
    
    private val flow = flow {
        while (true) {
            emit(getIsMetered())
            kotlinx.coroutines.delay(60000)
        }
    }
    override val isMeteredNetworkFlow: Flow<Boolean> = flow
    
    private fun getIsMetered(): Boolean {
        var networkCostManager: INetworkCostManager? = null

        try {
            val coInitializeHResult = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED)
            COMUtils.checkRC(coInitializeHResult)

            val pNetworkCostManager = PointerByReference()
            val coCreateInstanceHResult = Ole32.INSTANCE.CoCreateInstance(
                CLSID_NetworkListManager,
                null,
                WTypes.CLSCTX_ALL,
                IID_INetworkCostManager,
                pNetworkCostManager,
            )
            COMUtils.checkRC(coCreateInstanceHResult)
            
            networkCostManager = INetworkCostManager(pNetworkCostManager)
            val pCost = IntByReference()
            
            val getCostResult = networkCostManager.GetCost(pCost)
            COMUtils.checkRC(getCostResult)
            
            return (pCost.value and NLM_CONNECTION_COST_FIXED) != 0
        } catch (ex: COMException) {
            logger.warn(ex) { "Failed to get network status." }
            return false
        } finally {
            networkCostManager?.Release()
            Ole32.INSTANCE.CoUninitialize()
        }
    }

    override fun dispose() {
        
    }
    
    private class INetworkCostManager(pointer: PointerByReference) : Unknown(pointer.value) {
        @Suppress("FunctionName")
        fun GetCost(cost: IntByReference): HRESULT {
            return _invokeNativeObject(3, arrayOf(pointer, cost, null), HRESULT::class.java) as HRESULT
        }
    }
    
    @Suppress("unused")
    companion object {
        private val CLSID_NetworkListManager = Guid.CLSID("{DCB00C01-570F-4A9B-8D69-199FDBA5723B}")
        private val IID_INetworkCostManager = Guid.IID("{DCB00008-570F-4A9B-8D69-199FDBA5723B}")
        
        private const val NLM_CONNECTION_COST_UNKNOWN = 0
        private const val NLM_CONNECTION_COST_UNRESTRICTED = 0x1
        private const val NLM_CONNECTION_COST_FIXED = 0x2
        private const val NLM_CONNECTION_COST_VARIABLE = 0x4
        private const val NLM_CONNECTION_COST_OVERDATALIMIT = 0x10000
        private const val NLM_CONNECTION_COST_CONGESTED = 0x20000
        private const val NLM_CONNECTION_COST_ROAMING = 0x40000
        private const val NLM_CONNECTION_COST_APPROACHINGDATALIMIT = 0x80000
    }
}

actual fun createMeteredNetworkDetector(context: Context): MeteredNetworkDetector {
    return when (currentPlatformDesktop()) {
        is Platform.Windows -> WindowsMeteredNetworkDetector()
        else -> object : MeteredNetworkDetector {
            override val isMeteredNetworkFlow: Flow<Boolean> = flowOf(false)
            override fun dispose() { }
        }
    }
}

