package me.him188.ani.app.ui.foundation.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId

@Composable
fun OneshotTip(
    text: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector = Icons.Outlined.Info,
    onClose: () -> Unit,
) {
    val constraintSet = remember {
        ConstraintSet {
            val (iconRef, textRef, dismissRef) = createRefsFor("icon", "text", "dismiss")

            constrain(iconRef) {
                start.linkTo(parent.start, 16.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }

            constrain(textRef) {
                top.linkTo(parent.top, 12.dp)
                bottom.linkTo(parent.bottom, 12.dp)
                start.linkTo(iconRef.end, 16.dp)
                end.linkTo(dismissRef.start)
                width = Dimension.fillToConstraints
            }

            constrain(dismissRef) {
                top.linkTo(parent.top, 12.dp)
                bottom.linkTo(parent.bottom, 12.dp)
                end.linkTo(parent.end)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier,
    ) {
        ConstraintLayout(
            constraintSet = constraintSet,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.layoutId("icon"),
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.layoutId("dismiss")
            ) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.layoutId("text")
            )
        }
    }

}