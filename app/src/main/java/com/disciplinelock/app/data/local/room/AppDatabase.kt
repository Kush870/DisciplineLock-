package com.disciplinelock.app.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UsageLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
}
