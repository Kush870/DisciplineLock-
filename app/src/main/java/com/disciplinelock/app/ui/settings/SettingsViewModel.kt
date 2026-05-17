package com.disciplinelock.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinelock.app.data.local.DataStoreManager
import com.disciplinelock.app.data.local.room.UsageDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val usageDao: UsageDao
) : ViewModel() {

    val dailyLimitMinutes = dataStoreManager.dailyLimitMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val isHardcoreMode = dataStoreManager.hardcoreModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val currentDateString: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }

    val isLockedByHardcore: StateFlow<Boolean> = combine(
        dataStoreManager.hardcoreModeFlow,
        usageDao.getAllUsageLogs().map { logs ->
            val todayLog = logs.find { it.dateString == currentDateString }
            todayLog?.totalTimeInSeconds ?: 0L
        }
    ) { hardcore, todayUsageSeconds ->
        hardcore && todayUsageSeconds > 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateDailyLimit(minutes: Int) {
        viewModelScope.launch {
            if (!isLockedByHardcore.value) {
                dataStoreManager.setDailyLimit(minutes)
            }
        }
    }

    fun toggleHardcoreMode(enabled: Boolean) {
        viewModelScope.launch {
            if (!isLockedByHardcore.value) {
                dataStoreManager.setHardcoreMode(enabled)
            }
        }
    }
}
