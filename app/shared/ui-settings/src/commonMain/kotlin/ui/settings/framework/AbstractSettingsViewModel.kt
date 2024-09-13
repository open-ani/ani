package me.him188.ani.app.ui.settings.framework

import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.repository.Settings
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.produceState
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class AbstractSettingsViewModel : AbstractViewModel() {

    ///////////////////////////////////////////////////////////////////////////
    // Settings
    ///////////////////////////////////////////////////////////////////////////
    // TODO: Move extensions for Settings to top-level when context parameters are available.

    // starts eagerly
    fun <Value : Placeholder, Placeholder> Settings<Value>.stateInBackground(
        placeholder: Placeholder,
        backgroundScope: CoroutineScope = this@AbstractSettingsViewModel.backgroundScope,
    ): BaseSettingsState<Value, Placeholder> {
        return BaseSettingsState(
            flow.produceState(placeholder, backgroundScope),
            onUpdate = { set(it) },
            placeholder,
            backgroundScope,
        )
    }

    private inline fun <T> propertyDelegateProvider(
        crossinline createProperty: (property: KProperty<*>) -> T,
    ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
        return PropertyDelegateProvider { _, property ->
            val value = createProperty(property)
            ReadOnlyProperty { _, _ ->
                value
            }
        }
    }

    @Deprecated(
        "Use stateInBackground instead",
        ReplaceWith("settings.stateInBackground(placeholder)"),
    )
    fun <Value : Placeholder, Placeholder> settings(
        settings: Settings<Value>,
        placeholder: Placeholder
    ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, BaseSettingsState<Value, Placeholder>>> {
        return propertyDelegateProvider {
            settings.stateInBackground(placeholder)
        }
    }
}