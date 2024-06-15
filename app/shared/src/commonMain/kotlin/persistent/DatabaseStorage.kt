package me.him188.ani.app.persistent

import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.AniDatabase
import me.him188.ani.app.platform.Context

expect fun Context.createDatabaseBuilder(): RoomDatabase.Builder<AniDatabase>