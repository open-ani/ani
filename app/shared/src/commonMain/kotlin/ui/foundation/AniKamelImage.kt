package me.him188.animationgarden.app.ui.foundation

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import io.kamel.core.Resource

@Composable
fun AniKamelImage(
    resource: Resource<Painter>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    onLoading: @Composable (BoxScope.(Float) -> Unit)? = { LoadingIndicator(it) },
    onFailure: @Composable (BoxScope.(Throwable) -> Unit)? = { BrokenImagePlaceholder() },
    contentAlignment: Alignment = Alignment.Center,
    animationSpec: FiniteAnimationSpec<Float>? = tween(400),
) {
    return io.kamel.image.KamelImage(
        resource,
        contentDescription,
        modifier,
        alignment,
        contentScale,
        alpha,
        colorFilter,
        onLoading,
        onFailure,
        contentAlignment,
        animationSpec
    )
}


@Composable
fun LoadingIndicator(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxSize()
            .background(Color.LightGray)
    ) {
        CircularProgressIndicator(progress, Modifier.align(Alignment.Center))
    }
}

@Composable
fun BrokenImagePlaceholder(modifier: Modifier = Modifier) {
    return IconImagePlaceholder(Icons.Outlined.BrokenImage, modifier)
}

@Composable
fun IconImagePlaceholder(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.fillMaxSize()
            .background(Color.LightGray)
    ) {
        Icon(icon, "Broken", Modifier.align(Alignment.Center))
    }
}