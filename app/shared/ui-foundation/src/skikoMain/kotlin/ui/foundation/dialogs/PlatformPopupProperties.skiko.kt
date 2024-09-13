package me.him188.ani.app.ui.foundation.dialogs

import androidx.compose.ui.window.PopupProperties

@Suppress("FunctionName")
actual fun PlatformPopupPropertiesImpl(
    focusable: Boolean,
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    // Android-only:
    excludeFromSystemGesture: Boolean,
    clippingEnabled: Boolean,
    // Desktop-only:
    usePlatformInsets: Boolean,
): PopupProperties {
    return PopupProperties(
        focusable = focusable,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        clippingEnabled = clippingEnabled,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
        usePlatformInsets = usePlatformInsets,
    )
}