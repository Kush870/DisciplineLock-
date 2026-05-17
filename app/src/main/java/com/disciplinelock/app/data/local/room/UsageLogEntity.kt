package com.disciplinelock.app.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val totalTimeInSeconds: Long,
    val streakMaintained: Boolean
)
