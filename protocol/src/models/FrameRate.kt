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

import kotlinx.serialization.Serializable

@Serializable
data class FrameRate(
    val value: Int,
) {
    companion object {
        val F60 = FrameRate(60)

        fun tryParse(text: String): FrameRate? {
            // TODO: 2022/8/4 optimize
            if (text.contains("@60")) {
                return F60
            }
            if (text.contains("1080P60")) {
                return F60
            }
            if (text.contains("2160P60")) {
                return F60
            }
            if (text.contains("60FPS")) {
                return F60
            }
            if (text.contains("60 FPS")) {
                return F60
            }
            return null
        }
    }
}