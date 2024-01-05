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

package me.him188.animationgarden.database.impl.xodus.impl

import com.jetbrains.teamsys.dnq.database.TransientEntityStoreImpl
import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.*
import kotlinx.dnq.query.eq
import kotlinx.dnq.query.firstOrNull
import kotlinx.dnq.query.query
import me.him188.animationgarden.server.database.NameIndexes
import me.him188.animationgarden.datasources.api.Subject

internal class XdSubjectIndex(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdSubjectIndex>()

    val officialName: String by xdRequiredStringProp()
    val chineseName: String by xdRequiredStringProp()

    val dataSourceId: String by xdRequiredStringProp()
    val episodeCount: Int by xdIntProp()
    val ratingScore: Double by xdDoubleProp()
    val ratingCount: Int by xdIntProp()
    val rank: Int by xdIntProp()
    val sourceUrl: String by xdRequiredStringProp()
    val images: String by xdRequiredStringProp()
}

internal class XdSubject

internal class NameIndexesImpl(
    private val xd: TransientEntityStoreImpl,
) : NameIndexes {
    override fun getByOfficialName(officialName: String): NameIndexes.GetByIdResult {
        return xd.transactional {
            val entity = XdSubjectIndex.query(XdSubjectIndex::officialName eq officialName).firstOrNull()
                ?: return@transactional NameIndexes.GetByIdResult.NotFound
            return@transactional NameIndexes.GetByIdResult.Success(
                Subject(
                    id = entity.xdId,
                    officialName = entity.officialName,
                    chineseName = entity.chineseName,
                    dataSourceId = entity.dataSourceId,
                    episodeCount = entity.episodeCount,
                    ratingScore = entity.ratingScore,
                    ratingCount = entity.ratingCount,
                    rank = entity.rank,
                    sourceUrl = entity.sourceUrl,
                    images = TODO("images"),
                )
            )
        }
    }

    override fun insert(subject: Subject): NameIndexes.InsertResult {

    }
}