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

package me.him188.animationgarden.desktop.i18n

import androidx.compose.runtime.Stable
import java.util.*

@Stable
class ResourceBundle(
    private val delegate: java.util.ResourceBundle,
) {
    @Stable
    fun getString(name: String): String {
        return delegate.getString(name)
    }

    companion object {
        fun load(locale: Locale = Locale.getDefault()): ResourceBundle = ResourceBundle(
            try {
                PropertyResourceBundle.getBundle(
                    "i18n.app",
                    locale,
                    ResourceBundle::class.java.classLoader
                )
            } catch (firstE: MissingResourceException) {
                try {
                    PropertyResourceBundle.getBundle(
                        "i18n.app",
                        Locale.SIMPLIFIED_CHINESE,
                        ResourceBundle::class.java.classLoader
                    )
                } catch (e: Throwable) {
                    e.addSuppressed(firstE)
                    throw e
                }
            }
        )
    }
}