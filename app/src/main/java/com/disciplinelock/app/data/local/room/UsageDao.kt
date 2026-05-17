package com.disciplinelock.app.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageLog(usageLog: UsageLogEntity)

    @Query("SELECT * FROM usage_logs WHERE dateString = :date LIMIT 1")
    suspend fun getUsageForDate(date: String): UsageLogEntity?

    @Query("SELECT * FROM usage_logs ORDER BY dateString DESC")
    fun getAllUsageLogs(): Flow<List<UsageLogEntity>>
}
