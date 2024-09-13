package me.him188.ani.app.data.persistent

import androidx.room.RoomDatabase
import me.him188.ani.app.data.persistent.database.AniDatabase
import me.him188.ani.app.platform.Context

actual fun Context.createDatabaseBuilder(): RoomDatabase.Builder<AniDatabase> {
    TODO("Not yet implemented")
}