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

package me.him188.ani.app.i18n

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import me.him188.ani.R
import me.him188.ani.app.platform.Context
import java.util.Properties

@Stable
actual fun loadResourceBundle(
    context: Context,
    locale: Locale
): ResourceBundle = ResourceBundleImplByProperties.load(context, locale)

private fun ResourceBundleImplByProperties.Companion.load(
    context: Context,
    locale: Locale = Locale.current
): ResourceBundle {
    val properties = Properties().apply {
        val id = when {
            locale.language.contains("zh", ignoreCase = true) -> {
                when {
                    locale.region.contains("CN", ignoreCase = true) -> {
                        R.raw.app_zh_cn
                    }
                    locale.region.contains("TW", ignoreCase = true) ||
                            locale.region.contains("HK", ignoreCase = true)
                    -> {
                        R.raw.app_zh_hk
                    }
                    else -> R.raw.app_zh_cn
                }
            }
            else -> {
                R.raw.app_en
            }
        }
        context.resources.openRawResource(id).use {
            load(it)
        }
    }
    return ResourceBundleImplByProperties(properties)
}