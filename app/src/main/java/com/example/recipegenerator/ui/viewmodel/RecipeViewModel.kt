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
import kotlinx.coroutines.flow.*

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<MealDto>>(emptyList())
    val searchResults: StateFlow<List<MealDto>> = _searchResults
    val favoriteRecipes: StateFlow<List<RecipeEntity>> = repository.favoriteRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val searchResultsWithFavorites: StateFlow<List<MealDto>> = _searchResults
        .combine(favoriteRecipes) { remoteMeals, localFavorites ->
            remoteMeals.map { remote ->
                val isFav = localFavorites.any { it.remoteId == remote.idMeal }
                remote.copy(isFavorite = isFav)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _navigateToDetails = MutableStateFlow<RecipeEntity?>(null)
    val navigateToDetails: StateFlow<RecipeEntity?> = _navigateToDetails
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _selectedMealDetails = MutableStateFlow<MealDto?>(null)
    val selectedMealDetails: StateFlow<MealDto?> = _selectedMealDetails

    init {
        searchRecipes("s")
    }

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

    private val _selectedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val selectedRecipe: StateFlow<RecipeEntity?> = _selectedRecipe.asStateFlow()

    fun selectMeal(mealId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val localRecipe = repository.getLocalRecipeById(mealId)

                if (localRecipe != null) {
                    _selectedRecipe.value = localRecipe
                    _navigateToDetails.value = localRecipe
                } else {
                    val response = repository.getRecipeById(mealId)
                    val remoteMeal = response.meals?.firstOrNull()

                    if (remoteMeal != null) {
                        val entity = remoteMeal.toEntity()
                        _selectedRecipe.value = entity
                        _navigateToDetails.value = entity
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMealDetails(mealId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMealDetails(mealId)
                _selectedMealDetails.value = response.meals?.firstOrNull()
            } catch (e: Exception) {
                _errorMessage.value = "Could not load details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            if (query != "s") _selectedCategory.value = null
            _isLoading.value = true
            try {
                val response = repository.searchRecipesByName(query)
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            val existingFavorite = repository.getLocalRecipeByRemoteId(recipe.remoteId)
            repository.toggleFavorite(recipe)

            val favoriteMessage = if (existingFavorite != null) {
                "Removed From Favorites"
            } else {
                "Added to Favorites"
            }
            _snackbarMessage.emit(favoriteMessage)
        }
    }

    fun onNavigated() {
        _navigateToDetails.value = null
        _selectedRecipe.value = null
    }

    fun clearError() {
        _errorMessage.value = null
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

    private val _selectedMeal = MutableStateFlow<MealDto?>(null)
    val selectedMeal: StateFlow<MealDto?> = _selectedMeal

    fun clearSelectedMeal() {
        _selectedMeal.value = null
        _selectedRecipe.value = null
    }

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun filterByCategory(categoryName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedCategory.value = categoryName
            try {
                val response = repository.filterByCategory(categoryName)
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load $categoryName recipes: ${e.message}."
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetToDefault() {
        _selectedCategory.value = null
        searchRecipes("s")
    }
}

fun MealDto.toEntity(): RecipeEntity {
    fun pair(ing: String?, meas: String?): String? {
        return if (!ing.isNullOrBlank()) {
            if (!meas.isNullOrBlank()) "$meas $ing" else ing
        } else null
    }

    val ingredientList = listOfNotNull(
        pair(strIngredient1, strMeasure1),
        pair(strIngredient2, strMeasure2),
        pair(strIngredient3, strMeasure3),
        pair(strIngredient4, strMeasure4),
        pair(strIngredient5, strMeasure5),
        pair(strIngredient6, strMeasure6),
        pair(strIngredient7, strMeasure7),
        pair(strIngredient8, strMeasure8),
        pair(strIngredient9, strMeasure9),
        pair(strIngredient10, strMeasure10),
        pair(strIngredient11, strMeasure11),
        pair(strIngredient12, strMeasure12),
        pair(strIngredient13, strMeasure13),
        pair(strIngredient14, strMeasure14),
        pair(strIngredient15, strMeasure15),
        pair(strIngredient16, strMeasure16),
        pair(strIngredient17, strMeasure17),
        pair(strIngredient18, strMeasure18),
        pair(strIngredient19, strMeasure19),
        pair(strIngredient20, strMeasure20)
    )

    return RecipeEntity(
        remoteId = this.idMeal,
        name = this.strMeal,
        imageUrl = this.strMealThumb ?: "",
        category = this.strCategory ?: "",
        area = this.strArea ?: "",
        ingredients = ingredientList.joinToString("\n● ", "● "),
        instruction = this.strInstructions ?: "No instructions available",
        isFavorite = false
    )
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