package me.him188.ani.app.ui.foundation.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

const val PHONE_LANDSCAPE = "spec:id=reference_phone,shape=Normal,height=411,width=891,unit=dp,dpi=420"

@Preview(device = Devices.PIXEL_TABLET)
@Preview(device = Devices.PIXEL_TABLET, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewTabletLightDark
