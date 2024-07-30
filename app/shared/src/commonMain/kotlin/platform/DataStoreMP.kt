package me.him188.ani.app.platform

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.him188.ani.utils.io.SystemPath

expect interface DataStoreSerializer<T>

expect fun <T> KSerializer<T>.asDataStoreSerializer(
    defaultValue: () -> T,
    format: Json = DataStoreJson,
): DataStoreSerializer<T>

// Datastore 忘了给 expect 加 default constructor
expect fun <T> ReplaceFileCorruptionHandler(produceNewData: (CorruptionException) -> T): ReplaceFileCorruptionHandler<T>

fun <T> DataStoreFactory.create(
    serializer: KSerializer<T>,
    defaultValue: () -> T,
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = listOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceFile: () -> SystemPath
): DataStore<T> {
    return create(
        serializer = serializer.asDataStoreSerializer(defaultValue),
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = scope,
        produceFile = produceFile,
    )
}

expect fun <T> DataStoreFactory.create(
    serializer: DataStoreSerializer<T>,
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = listOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceFile: () -> SystemPath
): DataStore<T>

val DataStoreJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}
