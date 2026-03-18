package com.example.recipegenerator.data.repository

import com.example.recipegenerator.data.dao.AppSettingsDao
import com.example.recipegenerator.data.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsRepository(private val dao: AppSettingsDao) {

    val settings: Flow<AppSettingsEntity> = dao.getSettings().map {
        it ?: AppSettingsEntity()
    }

    suspend fun setDarkMode(enabled: Boolean) {
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.saveSettings(current.copy(isDarkMode = enabled))
    }

    suspend fun setFontSize(size: String) {
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.saveSettings(current.copy(fontSize = size))
    }
}