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
        fun load(): ResourceBundle = ResourceBundle(
            PropertyResourceBundle.getBundle(
                "i18n.app",
                Locale.getDefault(),
                ResourceBundle::class.java.classLoader
            )
        )
    }
}