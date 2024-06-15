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

package me.him188.ani.app.platform

import java.awt.Desktop
import java.io.File

object FileOpener {

    /**
     * 在 Windows 资源管理器或 macOS Finder 中打开文件所在目录, 并高亮该文件
     */
    fun openInFileBrowser(file: File) {
        if (highlightFile(file)) return

        if (file.isDirectory) {
            Desktop.getDesktop().open(file)
            return
        }
        Desktop.getDesktop().open(file.parentFile)
    }

    private fun highlightFile(file: File): Boolean {
        if (!file.exists()) {
            return false
        }

        return try {
            when (Platform.currentPlatform) {
                is Platform.Windows -> {
                    // Windows
                    val command = "explorer /select,\"${file.absolutePath}\""
                    Runtime.getRuntime().exec(command)
                    true
                }

                is Platform.MacOS -> {
                    // macOS
                    val command = "open -R \"${file.absolutePath}\""
                    Runtime.getRuntime().exec(command)
                    true
                }

                else -> false
            }
        } catch (e: Throwable) {
            false
        }
    }
}