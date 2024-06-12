package me.him188.ani.app.persistent

import androidx.room.Room
import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.AniDatabase
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.DesktopContext

actual val Context.databaseBuilder: RoomDatabase.Builder<AniDatabase>
    get() {
        this as DesktopContext
        return Room.databaseBuilder<AniDatabase>(
            name = dataDir.resolve("ani_room_database.db").absolutePath,
        )
    }