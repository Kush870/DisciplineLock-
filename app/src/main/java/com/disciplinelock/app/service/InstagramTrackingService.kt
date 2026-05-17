package com.disciplinelock.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.disciplinelock.app.data.local.DataStoreManager
import com.disciplinelock.app.data.local.room.UsageDao
import com.disciplinelock.app.data.local.room.UsageLogEntity
import com.disciplinelock.app.domain.StreakEngine
import com.disciplinelock.app.widget.DisciplineWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class InstagramTrackingService : AccessibilityService() {

    @Inject
    lateinit var usageDao: UsageDao

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var streakEngine: StreakEngine

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var currentLimitMinutes: Int = 30
    private var instagramStartTime: Long = 0L
    private var isInstagramForeground = false

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val TAG = "InstagramTracker"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceScope.launch {
            streakEngine.evaluateCatchUp()
            dataStoreManager.dailyLimitMinutesFlow.collect { limit ->
                currentLimitMinutes = limit
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        serviceScope.launch { streakEngine.evaluateCatchUp() }
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == INSTAGRAM_PACKAGE) {
            checkLimitAndBlock()
            
            if (!isInstagramForeground) {
                isInstagramForeground = true
                instagramStartTime = System.currentTimeMillis()
                Log.d(TAG, "Instagram opened at $instagramStartTime")
            }
        } else {
            if (isInstagramForeground) {
                isInstagramForeground = false
                val endTime = System.currentTimeMillis()
                val durationInSeconds = (endTime - instagramStartTime) / 1000
                Log.d(TAG, "Instagram closed. Duration: $durationInSeconds seconds")

                if (durationInSeconds > 0) {
                    logUsageToDatabase(durationInSeconds)
                }
            }
        }
    }

    private fun logUsageToDatabase(durationInSeconds: Long) {
        serviceScope.launch {
            val dateString = getCurrentDateString()
            val existingLog = usageDao.getUsageForDate(dateString)

            if (existingLog != null) {
                val newTotal = existingLog.totalTimeInSeconds + durationInSeconds
                usageDao.insertUsageLog(existingLog.copy(totalTimeInSeconds = newTotal))
                updateWidget(newTotal, currentLimitMinutes * 60L)
            } else {
                val newLog = UsageLogEntity(
                    dateString = dateString,
                    totalTimeInSeconds = durationInSeconds,
                    streakMaintained = true
                )
                usageDao.insertUsageLog(newLog)
                updateWidget(durationInSeconds, currentLimitMinutes * 60L)
            }
        }
    }

    private fun updateWidget(usageSeconds: Long, limitSeconds: Long) {
        val intent = Intent(this, DisciplineWidgetProvider::class.java).apply {
            action = DisciplineWidgetProvider.ACTION_UPDATE_WIDGET
            putExtra(DisciplineWidgetProvider.EXTRA_USAGE_MINUTES, (usageSeconds / 60).toInt())
            putExtra(DisciplineWidgetProvider.EXTRA_LIMIT_MINUTES, (limitSeconds / 60).toInt())
        }
        sendBroadcast(intent)
    }

    private fun checkLimitAndBlock() {
        serviceScope.launch {
            val dateString = getCurrentDateString()
            val existingLog = usageDao.getUsageForDate(dateString)
            val totalSeconds = existingLog?.totalTimeInSeconds ?: 0L
            val limitSeconds = currentLimitMinutes * 60L

            if (totalSeconds >= limitSeconds) {
                Log.d(TAG, "Limit reached. Blocking Instagram.")
                val intent = Intent(this@InstagramTrackingService, com.disciplinelock.app.ui.blocker.BlockerActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
        }
    }

    private fun getCurrentDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
