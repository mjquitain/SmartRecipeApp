package com.example.recipegenerator.data.dao

import androidx.room.*
import com.example.recipegenerator.data.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?
}