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