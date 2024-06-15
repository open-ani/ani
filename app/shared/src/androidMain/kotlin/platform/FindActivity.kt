package me.him188.ani.app.platform

import android.app.Activity
import android.content.ContextWrapper


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
