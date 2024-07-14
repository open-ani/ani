package me.him188.ani.app.ui.subject.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import kotlin.coroutines.CoroutineContext

@Stable
class EditableRatingState(
    ratingInfo: Flow<RatingInfo>,
    selfRatingInfo: Flow<SelfRatingInfo>,
    private val setSelfRating: suspend (RateRequest) -> Unit,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    val ratingInfo by ratingInfo.produceState(RatingInfo.Empty)
    val selfRatingInfo by selfRatingInfo.produceState(SelfRatingInfo.Empty)

    var showRatingDialog by mutableStateOf(false)
        private set

    fun startEdit() {
        showRatingDialog = true
    }

    fun cancelEdit() {
        showRatingDialog = false
    }

    private val tasker = MonoTasker(backgroundScope)
    val isUpdatingRating get() = tasker.isRunning
    fun updateRating(rateRequest: RateRequest) {
        tasker.launch {
            setSelfRating(rateRequest)
            showRatingDialog = false
        }
    }
}

@Composable
fun EditableRating(
    state: EditableRatingState,
    modifier: Modifier = Modifier,
) {
    if (state.showRatingDialog) {
        val selfRatingInfo = state.selfRatingInfo
        RatingEditorDialog(
            remember(selfRatingInfo) {
                RatingEditorState(
                    initialScore = selfRatingInfo.score,
                    initialComment = selfRatingInfo.comment ?: "",
                    initialIsPrivate = selfRatingInfo.isPrivate,
                )
            },
            onDismissRequest = {
                state.cancelEdit()
            },
            onRate = { state.updateRating(it) },
            isLoading = state.isUpdatingRating,
        )
    }
    Rating(
        rating = state.ratingInfo,
        selfRatingScore = state.selfRatingInfo.score,
        onClick = { state.startEdit() },
        modifier = modifier,
    )
}

@Composable
fun Rating(
    rating: RatingInfo,
    selfRatingScore: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiary) {
        Column {
            if (selfRatingScore != 0) {
                Row(Modifier.padding(horizontal = 2.dp).align(Alignment.End)) {
                    Text(
                        remember(selfRatingScore) { "我的评分: $selfRatingScore" },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
            Row(
                modifier.clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RatingScoreText(
                    score = remember(rating.score) {
                        if (!rating.score.contains(".")) {
                            "${rating.score}.0"
                        } else rating.score
                    },
                )

                Column(Modifier.padding(start = 8.dp), horizontalAlignment = Alignment.End) {
                    FiveRatingStars(score = rating.scoreFloat.toInt(), color = LocalContentColor.current)
                    Text(
                        "${rating.total} 人评丨#${rating.rank}",
                        Modifier.padding(end = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
fun RatingScoreText(
    score: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight = FontWeight.ExtraBold,
) {
    Text(
        score,
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
fun FiveRatingStars(
    score: Int, // range 0..10
    starSize: Dp = 22.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy((-1).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            Icon(
                when {
                    score >= 2 -> Icons.Rounded.Star
                    score == 1 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                Modifier.size(starSize),
            )
            Icon(
                when {
                    score >= 4 -> Icons.Rounded.Star
                    score == 3 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                Modifier.size(starSize),
            )
            Icon(
                when {
                    score >= 6 -> Icons.Rounded.Star
                    score == 5 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                Modifier.size(starSize),
            )
            Icon(
                when {
                    score >= 8 -> Icons.Rounded.Star
                    score == 7 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                Modifier.size(starSize),
            )
            Icon(
                when {
                    score >= 10 -> Icons.Rounded.Star
                    score == 9 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                Modifier.size(starSize),
            )
        }
    }
}
