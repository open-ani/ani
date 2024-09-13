package me.him188.ani.app.data.persistent

import androidx.room.RoomDatabase
import me.him188.ani.app.data.persistent.database.AniDatabase
import me.him188.ani.app.platform.Context

expect fun Context.createDatabaseBuilder(): RoomDatabase.Builder<AniDatabase>