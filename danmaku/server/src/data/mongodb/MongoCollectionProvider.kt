package me.him188.ani.danmaku.server.data.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import me.him188.ani.danmaku.server.data.model.DanmakuModel
import me.him188.ani.danmaku.server.ktor.plugins.ServerJson
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries


interface MongoCollectionProvider {
    val danmakuTable: MongoCollection<DanmakuModel>
}

class MongoCollectionProviderImpl(
    connection: String
) : MongoCollectionProvider {
    private val client = MongoClient.create(MongoClientSettings.builder().apply {
        applyConnectionString(ConnectionString(connection))
        codecRegistry(
            CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                ServerCodecRegistry(ServerJson),
            )
        )
        uuidRepresentation(UuidRepresentation.STANDARD)
    }.build())

    private val db = client.getDatabase("ani-production")
    override val danmakuTable = db.getCollection<DanmakuModel>("danmaku")
}
