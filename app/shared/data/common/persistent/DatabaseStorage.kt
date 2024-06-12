package me.him188.ani.app.persistent

import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.AniDatabase
import me.him188.ani.app.platform.Context

expect val Context.databaseBuilder: RoomDatabase.Builder<AniDatabase>