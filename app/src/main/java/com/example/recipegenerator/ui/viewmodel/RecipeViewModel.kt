package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.data.repository.RecipeRepository
import com.example.recipegenerator.network.MealDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    // Local DB recipes
    val allRecipes: StateFlow<List<RecipeEntity>> = repository.allRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteRecipes: StateFlow<List<RecipeEntity>> = repository.favoriteRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // API search results
    private val _searchResults = MutableStateFlow<List<MealDto>>(emptyList())
    val searchResults: StateFlow<List<MealDto>> = _searchResults

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // LOCAL CRUD
    fun insert(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.insert(recipe)
        }
    }

    fun update(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.update(recipe)
        }
    }

    fun delete(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.delete(recipe)
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe)
        }
    }

    // REMOTE - search MealDB API
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.searchRecipesByName(query)
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByIngredient(ingredient: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.filterByIngredient(ingredient)
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}