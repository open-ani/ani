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

package me.him188.animationgarden.database.impl.xodus

import com.jetbrains.teamsys.dnq.database.TransientEntityStoreImpl
import kotlinx.dnq.XdModel
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import me.him188.animationgarden.database.impl.xodus.impl.UsersImpl
import me.him188.animationgarden.server.database.Users
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

class XodusDatabaseBackend(
    dbDir: File,
) {
    private val entityStore: TransientEntityStoreImpl = StaticStoreContainer.init(
        dbFolder = dbDir,
        entityStoreName = "db",
    )

    val koinModule: Module = module {
        single<Users> { UsersImpl(entityStore) }
    }

    // 注意顺序, init 要放在 koinModule 初始化后面, 因为 `UsersImpl` 等会用到 `XdModel`
    init {
        initMetaData(XdModel.hierarchy, entityStore)
    }
}