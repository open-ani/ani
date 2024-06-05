package me.him188.ani.danmaku.server.data.mongodb

import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult

fun UpdateResult.checkUpdated() {
    if (matchedCount == 0L && upsertedId == null) {
        throw IllegalStateException("Update failed")
    }
}

fun InsertOneResult.checkUpdated(): String {
    val insertedId = insertedId ?: throw IllegalStateException("Insert failed")
    return insertedId.asString().value
}