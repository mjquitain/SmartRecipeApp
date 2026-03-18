package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(private val repository: AppSettingsRepository) : ViewModel() {

    val settings = repository.settings
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            com.example.recipegenerator.data.entity.AppSettingsEntity()
        )

    fun setFontSize(size: String) {
        viewModelScope.launch { repository.setFontSize(size) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }
}

class AppSettingsViewModelFactory(private val repository: AppSettingsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}