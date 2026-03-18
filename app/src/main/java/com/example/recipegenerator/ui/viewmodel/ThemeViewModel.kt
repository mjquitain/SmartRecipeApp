package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val repository: AppSettingsRepository) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = repository.settings
        .map { it.isDarkMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }
}

class ThemeViewModelFactory(private val repository: AppSettingsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}