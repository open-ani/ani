package me.him188.ani.app.data.persistent

import androidx.room.Room
import androidx.room.RoomDatabase
import me.him188.ani.app.data.persistent.database.AniDatabase
import me.him188.ani.app.data.persistent.database.AniDatabaseConstructor
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.asIosContext
import me.him188.ani.utils.io.absolutePath
import me.him188.ani.utils.io.resolve

actual fun Context.createDatabaseBuilder(): RoomDatabase.Builder<AniDatabase> {
    this.asIosContext()
    return Room.databaseBuilder<AniDatabase>(
        name = files.dataDir.resolve("ani_room_database.db").absolutePath,
    ) {
        AniDatabaseConstructor.initialize()
    }
}