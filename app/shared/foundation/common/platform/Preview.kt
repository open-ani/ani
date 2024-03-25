@file:Suppress("PackageDirectoryMismatch")

package androidx.compose.desktop.ui.tooling.preview

import androidx.annotation.FloatRange
import androidx.annotation.IntRange


@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Repeatable
annotation class Preview(
    val name: String = "",
    val group: String = "",
    @IntRange(from = 1) val apiLevel: Int = -1,
    // TODO(mount): Make this Dp when they are inline classes
    val widthDp: Int = -1,
    // TODO(mount): Make this Dp when they are inline classes
    val heightDp: Int = -1,
    val locale: String = "",
    @FloatRange(from = 0.01) val fontScale: Float = 1f,
    val showSystemUi: Boolean = false,
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0,
//    @UiMode val uiMode: Int = 0,
//    @Device val device: String = Devices.DEFAULT,
//    @Wallpaper val wallpaper: Int = Wallpapers.NONE,
)