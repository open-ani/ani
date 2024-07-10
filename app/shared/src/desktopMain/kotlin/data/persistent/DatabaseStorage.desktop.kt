package me.him188.ani.app.data.persistent

import androidx.room.Room
import androidx.room.RoomDatabase
import me.him188.ani.app.data.persistent.database.AniDatabase
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.DesktopContext

actual fun Context.createDatabaseBuilder(): RoomDatabase.Builder<AniDatabase> {
    this as DesktopContext
    return Room.databaseBuilder<AniDatabase>(
        name = dataDir.resolve("ani_room_database.db").absolutePath,
    )
}