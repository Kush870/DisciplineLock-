package com.disciplinelock.app.domain

import com.disciplinelock.app.data.local.DataStoreManager
import com.disciplinelock.app.data.local.room.UsageDao
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakEngine @Inject constructor(
    private val usageDao: UsageDao,
    private val dataStoreManager: DataStoreManager
) {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun evaluateCatchUp() {
        val todayStr = formatter.format(Date())
        val lastEvaluatedDateStr = dataStoreManager.lastEvaluatedDateFlow.first()

        if (lastEvaluatedDateStr == todayStr) {
            // Already evaluated today, do nothing.
            return
        }

        if (lastEvaluatedDateStr.isEmpty()) {
            // First time ever evaluating, set to yesterday so it evaluates starting today.
            val yesterdayStr = getPreviousDateString(Date())
            dataStoreManager.updateEvaluationState(yesterdayStr, 0, 100)
            return
        }

        val lastEvaluatedDate = formatter.parse(lastEvaluatedDateStr) ?: return
        var currentStreak = dataStoreManager.currentStreakFlow.first()
        var currentScore = dataStoreManager.disciplineScoreFlow.first()
        val limitMinutes = dataStoreManager.dailyLimitMinutesFlow.first()

        // Iterate through all days from lastEvaluatedDate + 1 up to yesterday
        val cal = Calendar.getInstance()
        cal.time = lastEvaluatedDate
        cal.add(Calendar.DAY_OF_YEAR, 1)

        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        while (cal.get(Calendar.YEAR) < yesterday.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) <= yesterday.get(Calendar.DAY_OF_YEAR))
        ) {
            val evalDateStr = formatter.format(cal.time)
            val usageLog = usageDao.getUsageForDate(evalDateStr)
            val usageSeconds = usageLog?.totalTimeInSeconds ?: 0L
            val limitSeconds = limitMinutes * 60L

            if (usageSeconds <= limitSeconds) {
                // Successful day
                currentStreak++
                currentScore = (currentScore + 5).coerceAtMost(100)
            } else {
                // Failed day
                currentStreak = 0
                currentScore = (currentScore - 20).coerceAtLeast(0)
            }

            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Update the evaluated state up to yesterday
        val newEvaluatedDateStr = formatter.format(yesterday.time)
        dataStoreManager.updateEvaluationState(newEvaluatedDateStr, currentStreak, currentScore)
    }

    private fun getPreviousDateString(date: Date): String {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return formatter.format(cal.time)
    }
}
