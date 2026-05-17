package com.disciplinelock.app.ui.blocker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disciplinelock.app.data.local.DataStoreManager
import com.disciplinelock.app.data.local.room.UsageDao
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
class BlockerViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val currentDateString: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }

    val canUseEmergencyUnlock: StateFlow<Boolean> = dataStoreManager.emergencyUnlockLastUsedDateFlow
        .map { lastUsedDate ->
            lastUsedDate != currentDateString
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun useEmergencyUnlock() {
        viewModelScope.launch {
            dataStoreManager.useEmergencyUnlock(currentDateString)
        }
    }
}
