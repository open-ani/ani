/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.nameCnOrName
import me.him188.ani.app.ui.rating.RatingText

@Immutable
class SubjectPreviewItemInfo(
    val id: Int,
    val imageUrl: String,
    val title: String,
    val tags: String,
    val staff: String,
    val actors: String,
    val rating: RatingInfo,
) {
    companion object {
        fun compute(
            subjectInfo: SubjectInfo,
            roleSet: RoleSet,
        ): SubjectPreviewItemInfo {
            return SubjectPreviewItemInfo(
                id = subjectInfo.id,
                subjectInfo.imageLarge,
                subjectInfo.nameCnOrName,
                "", // tODO
                "",
                "",
                rating = subjectInfo.ratingInfo,
            )
        }
    }
}

@Composable
fun SubjectPreviewItem(
    onViewDetails: () -> Unit,
    onPlay: () -> Unit,
    info: SubjectPreviewItemInfo,
    modifier: Modifier = Modifier,
) {
    SubjectItemLayout(
        onClick = onViewDetails,
        image = {
            SubjectItemDefaults.Image(info.imageUrl)
        },
        title = { maxLines ->
            Text(info.title, maxLines = maxLines)
        },
        tags = {
            Text(info.tags, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        extraInfo = {
            Text(info.staff, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(info.actors, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        rating = {
            RatingText(info.rating)
        },
        actions = {
//            SubjectItemDefaults.ActionPlay(onPlay)
        },
        modifier,
    )
}
