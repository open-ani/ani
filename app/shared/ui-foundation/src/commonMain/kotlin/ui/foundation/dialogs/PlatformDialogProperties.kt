package me.him188.ani.app.ui.foundation.dialogs

import androidx.compose.ui.window.DialogProperties


@Suppress("FunctionName")
expect fun PlatformDialogPropertiesImpl(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = false,
    // Android-only:
    excludeFromSystemGesture: Boolean = true,
    // Skiko-only:
    usePlatformInsets: Boolean = true,
): DialogProperties

@Suppress("FunctionName")
fun PlatformDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = false,
    // Android-only:
    excludeFromSystemGesture: Boolean = true,
    clippingEnabled: Boolean = true,
    // Skiko-only:
    usePlatformInsets: Boolean = true,
): DialogProperties = PlatformDialogPropertiesImpl(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    excludeFromSystemGesture = excludeFromSystemGesture,
    usePlatformInsets = usePlatformInsets,
)
