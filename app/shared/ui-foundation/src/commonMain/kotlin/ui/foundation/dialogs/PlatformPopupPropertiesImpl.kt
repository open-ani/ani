package me.him188.ani.app.ui.foundation.dialogs

import androidx.compose.ui.window.PopupProperties

@Suppress("FunctionName")
expect fun PlatformPopupPropertiesImpl(
    focusable: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = false,
    // Android-only:
    excludeFromSystemGesture: Boolean = true,
    clippingEnabled: Boolean = true,
    // Desktop-only:
    usePlatformInsets: Boolean = true,
): PopupProperties

@Suppress("FunctionName")
fun PlatformPopupProperties(
    focusable: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = false,
    // Android-only:
    excludeFromSystemGesture: Boolean = true,
    clippingEnabled: Boolean = true,
    // Desktop-only:
    usePlatformInsets: Boolean = true,
): PopupProperties = PlatformPopupPropertiesImpl(
    focusable = focusable,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    excludeFromSystemGesture = excludeFromSystemGesture,
    clippingEnabled = clippingEnabled,
    usePlatformInsets = usePlatformInsets,
)
