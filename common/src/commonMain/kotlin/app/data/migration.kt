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

package me.him188.animationgarden.app.app.data

import mu.KotlinLogging
import java.io.File


object Migrations {
    private val logger = KotlinLogging.logger { }
    fun migrateFile(legacy: File, new: File) {
        if (new.exists()) return
        if (legacy.exists()) {
            new.parentFile?.mkdirs()
            legacy.copyTo(new)
            logger.warn { "Migrated '${legacy.absolutePath}' to '${new.absolutePath}'" }
        }
    }
}
