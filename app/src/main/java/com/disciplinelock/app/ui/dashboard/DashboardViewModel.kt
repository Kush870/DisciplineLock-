package com.disciplinelock.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinelock.app.data.local.DataStoreManager
import com.disciplinelock.app.data.local.room.UsageDao
import com.disciplinelock.app.domain.StreakEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageDao: UsageDao,
    private val dataStoreManager: DataStoreManager,
    private val streakEngine: StreakEngine
) : ViewModel() {

    init {
        viewModelScope.launch {
            streakEngine.evaluateCatchUp()
        }
    }

    private val currentDateString: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }

    val dailyUsageSeconds: StateFlow<Long> = usageDao.getAllUsageLogs()
        .map { logs ->
            val todayLog = logs.find { it.dateString == currentDateString }
            todayLog?.totalTimeInSeconds ?: 0L
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val dailyLimitMinutes: StateFlow<Int> = dataStoreManager.dailyLimitMinutesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 30
        )

    val currentStreak: StateFlow<Int> = dataStoreManager.currentStreakFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val disciplineScore: StateFlow<Int> = dataStoreManager.disciplineScoreFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 100
        )
}
