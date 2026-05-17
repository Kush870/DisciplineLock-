package com.disciplinelock.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val DAILY_LIMIT_MINUTES = intPreferencesKey("daily_limit_minutes")
        val DISCIPLINE_SCORE = intPreferencesKey("discipline_score")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val LAST_LIMIT_UPDATE_TIME = longPreferencesKey("last_limit_update_time")
        val EMERGENCY_UNLOCK_LAST_USED_DATE = androidx.datastore.preferences.core.stringPreferencesKey("emergency_unlock_last_used_date")
        val LAST_EVALUATED_DATE = androidx.datastore.preferences.core.stringPreferencesKey("last_evaluated_date")
        val HARDCORE_MODE_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("hardcore_mode_enabled")
    }

    val dailyLimitMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT_MINUTES] ?: 30 // Default 30 mins
    }
    
    val disciplineScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DISCIPLINE_SCORE] ?: 100
    }
    
    val hardcoreModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HARDCORE_MODE_ENABLED] ?: false
    }
    
    val currentStreakFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_STREAK] ?: 0
    }

    val emergencyUnlockLastUsedDateFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EMERGENCY_UNLOCK_LAST_USED_DATE] ?: ""
    }

    val lastEvaluatedDateFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_EVALUATED_DATE] ?: ""
    }

    suspend fun updateEvaluationState(newDate: String, newStreak: Int, newScore: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_EVALUATED_DATE] = newDate
            preferences[CURRENT_STREAK] = newStreak
            preferences[DISCIPLINE_SCORE] = newScore
            
            val bestStreak = preferences[BEST_STREAK] ?: 0
            if (newStreak > bestStreak) {
                preferences[BEST_STREAK] = newStreak
            }
        }
    }

    suspend fun setDailyLimit(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT_MINUTES] = minutes
            preferences[LAST_LIMIT_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun setHardcoreMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HARDCORE_MODE_ENABLED] = enabled
        }
    }

    suspend fun useEmergencyUnlock(currentDateString: String) {
        context.dataStore.edit { preferences ->
            val currentLimit = preferences[DAILY_LIMIT_MINUTES] ?: 30
            preferences[DAILY_LIMIT_MINUTES] = currentLimit + 5
            preferences[EMERGENCY_UNLOCK_LAST_USED_DATE] = currentDateString
            
            // Decrease discipline score
            val currentScore = preferences[DISCIPLINE_SCORE] ?: 100
            preferences[DISCIPLINE_SCORE] = (currentScore - 10).coerceAtLeast(0)
        }
    }
}
