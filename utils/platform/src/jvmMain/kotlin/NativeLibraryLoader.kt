/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.utils.platform

import java.io.File

object NativeLibraryLoader {
    fun getResourceDir(
        dirName: String,
    ): File {
        val platform = currentPlatform() as Platform.Desktop
        System.getProperty("compose.application.resources.dir")?.let {
            val file = File(it).resolve(dirName)
            if (file.exists()) {
                return file
            }
        }

        val arch = when (platform.arch) {
            Arch.X86_64 -> "x64"
            Arch.AARCH64 -> "arm64"
            Arch.ARMV7A, Arch.ARMV8A -> throw UnsatisfiedLinkError("Unsupported architecture: ${platform.arch}")
        }

        val triple = when (platform) {
            is Platform.MacOS -> "macos-$arch"
            is Platform.Windows -> "windows-$arch"
            is Platform.Linux -> "linux-$arch"
        }

        System.getProperty("user.dir")?.let { File(it) }?.resolve("../appResources/$triple")
            ?.resolve(dirName)
            ?.let {
                if (it.exists()) {
                    return it
                }
            }

        throw UnsatisfiedLinkError("Anitorrent resource directory not found")
    }
}
