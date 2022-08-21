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

package me.him188.animationgarden.app.i18n

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import me.him188.animationgarden.app.platform.Context
import java.util.*

actual fun loadResourceBundle(
    context: Context,
    locale: Locale
): ResourceBundle = ResourceBundleImpl.load(locale)

@Stable
private class ResourceBundleImpl(
    private val delegate: java.util.ResourceBundle,
) : ResourceBundle {
    @Stable
    override fun getString(name: String): String {
        return delegate.getString(name)
    }

    companion object {
        fun load(locale: Locale = Locale.current): ResourceBundle {
            val javaLocale = java.util.Locale.forLanguageTag(locale.toLanguageTag())
            return ResourceBundleImpl(
                try {
                    PropertyResourceBundle.getBundle(
                        "i18n.app",
                        javaLocale,
                        ResourceBundle::class.java.classLoader
                    )
                } catch (firstE: MissingResourceException) {
                    try {
                        PropertyResourceBundle.getBundle(
                            "i18n.app",
                            javaLocale,
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
}