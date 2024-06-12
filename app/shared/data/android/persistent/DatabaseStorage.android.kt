package me.him188.ani.app.persistent

import androidx.room.Room
import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.AniDatabase
import me.him188.ani.app.platform.Context

actual val Context.databaseBuilder: RoomDatabase.Builder<AniDatabase>
    get() {
        return Room.databaseBuilder<AniDatabase>(
            context = applicationContext,
            name = applicationContext.getDatabasePath("ani_room_database.db").absolutePath
        )
    }