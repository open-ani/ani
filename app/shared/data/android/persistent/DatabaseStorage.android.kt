package me.him188.ani.app.persistent

import androidx.room.Room
import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.AniDatabase
import me.him188.ani.app.platform.Context

actual fun Context.createDatabaseBuilder(): RoomDatabase.Builder<AniDatabase> {
    return Room.databaseBuilder<AniDatabase>(
        context = applicationContext,
        name = applicationContext.getDatabasePath("ani_room_database.db").absolutePath
    )
}