/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.android.activity

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import me.him188.animationgarden.app.AppTheme

@Composable
fun Activity.ImmerseStatusBar(systemBarsColor: Color = AppTheme.colorScheme.background) {
//    var systemUiVisibility = this.window.decorView.systemUiVisibility
//    systemUiVisibility =
//        systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//    systemUiVisibility =
//        systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//    this.window.decorView.systemUiVisibility = systemUiVisibility
    LaunchedEffect(systemBarsColor) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (systemBarsColor.luminance() > 0.5) {
                    // light background, use dark system bars
                    window.insetsController?.setSystemBarsAppearance(
                        APPEARANCE_LIGHT_STATUS_BARS or APPEARANCE_LIGHT_NAVIGATION_BARS,
                        APPEARANCE_LIGHT_STATUS_BARS or APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                } else {
                    window.insetsController?.setSystemBarsAppearance(
                        0,
                        APPEARANCE_LIGHT_STATUS_BARS or APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                if (systemBarsColor.luminance() > 0.5) {
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            }

            window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = systemBarsColor.copy().toArgb()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        window.insetsController?.hide(WindowInsets.Type.statusBars())
//    } else {
//        this.getWindow()
//            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    }
}
