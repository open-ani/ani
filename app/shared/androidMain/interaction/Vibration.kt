/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.interaction

import android.annotation.SuppressLint
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import me.him188.ani.app.platform.Context

@RequiresPermission(android.Manifest.permission.VIBRATE)
actual fun Context.vibrateIfSupported(strength: VibrationStrength) {
    val effect = strength.toEffect() ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService<VibratorManager>()?.vibrate(CombinedVibration.createParallel(effect))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(effect)
        } else {
            // not supported
        }
    }
}

private fun VibrationStrength.toEffect(): VibrationEffect? {
    return when (this) {
        VibrationStrength.TICK -> defaultTick
        VibrationStrength.CLICK -> defaultClick
        VibrationStrength.HEAVY_CLICK -> defaultHeavyClick
    }
}

private val defaultTick: VibrationEffect? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        createPredefined(VibrationEffect.EFFECT_TICK)
    } else {
        null
    }
}

private val defaultClick: VibrationEffect? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        createPredefined(VibrationEffect.EFFECT_CLICK)
    } else {
        null
    }
}

private val defaultHeavyClick: VibrationEffect? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
    } else {
        null
    }
}

@SuppressLint("NewApi")
private fun createPredefined(int: Int): VibrationEffect {
    return VibrationEffect.createPredefined(int)
}