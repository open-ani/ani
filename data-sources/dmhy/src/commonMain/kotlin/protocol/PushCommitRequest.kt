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

package me.him188.animationgarden.api.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import me.him188.animationgarden.api.model.Commit

@Serializable
data class PushCommitRequest(
    @ProtoNumber(1) val base: CommitRef,
    @ProtoNumber(2) val commit: @Polymorphic Commit,
    @ProtoNumber(3) val newData: EAppData,
    @ProtoNumber(4) val committer: Committer,
)

@Serializable
data class PushCommitResponse(
    @ProtoNumber(1) val result: PushCommitResult,
)

@Serializable
sealed class PushCommitResult {

    @Serializable
    class Success(
        @ProtoNumber(1) val newHeadRef: CommitRef,
    ) : PushCommitResult()

    @Serializable
    class OutOfDate(
        @ProtoNumber(2) val newHeadRef: CommitRef,
    ) : PushCommitResult()
}


@Serializable
class SyncRefRequest()

@Serializable
data class SyncRefResponse(
    @ProtoNumber(1) val ref: CommitRef
)
