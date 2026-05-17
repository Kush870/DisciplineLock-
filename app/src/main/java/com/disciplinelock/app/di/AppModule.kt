package com.disciplinelock.app.di

import android.content.Context
import androidx.room.Room
import com.disciplinelock.app.data.local.DataStoreManager
import com.disciplinelock.app.data.local.room.AppDatabase
import com.disciplinelock.app.data.local.room.UsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "discipline_lock_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUsageDao(appDatabase: AppDatabase): UsageDao {
        return appDatabase.usageDao()
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }
}
