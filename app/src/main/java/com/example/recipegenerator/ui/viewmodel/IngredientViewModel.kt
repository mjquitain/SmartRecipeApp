package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.entity.IngredientEntity
import com.example.recipegenerator.data.repository.IngredientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientViewModel(private val repository: IngredientRepository) : ViewModel() {

    // Exposes ingredient list as StateFlow
    val ingredients: StateFlow<List<IngredientEntity>> = repository.allIngredients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.insert(ingredient)
        }
    }

    fun update(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.update(ingredient)
        }
    }

    fun delete(ingredient: IngredientEntity) {
        viewModelScope.launch {
            repository.delete(ingredient)
        }
    }
}

// Needed for ViewModel constructor parameter (repository)
class IngredientViewModelFactory(private val repository: IngredientRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}