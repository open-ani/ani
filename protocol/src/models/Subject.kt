/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.shared.models

import me.him188.animationgarden.datasources.api.SubjectImages

class Subject(
    val name: String,
    val episodeCount: Int,
    /**
     * 平均评分
     */
    val ratingScore: Double,
    /**
     * 评分人数
     */
    val ratingCount: Int,
    val rank: Int,
    val sourceUrl: String, // 数据源
    val images: SubjectImages,
)