package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.data.repository.RecipeRepository
import com.example.recipegenerator.network.MealDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val _favoriteIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteIds: StateFlow<List<String>> = _favoriteIds.asStateFlow()

    // ─────────────────────────────────────────────
    // SEARCH / API STATE
    // ─────────────────────────────────────────────

    private val _searchResults = MutableStateFlow<List<MealDto>>(emptyList())
    val searchResults: StateFlow<List<MealDto>> = _searchResults

    // Favorite recipes from Room
    val favoriteRecipes: StateFlow<List<RecipeEntity>> = repository.favoriteRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val searchResultsWithFavorites: StateFlow<List<MealDto>> = _searchResults
        .combine(favoriteIds) { remoteMeals, cloudIds ->
            remoteMeals.map { it.copy(isFavorite = cloudIds.contains(it.idMeal)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─────────────────────────────────────────────
    // DETAIL / NAVIGATION STATE
    // ─────────────────────────────────────────────

    // Full MealDto details for the detail screen (from API lookup by ID)
    private val _selectedMealDetails = MutableStateFlow<MealDto?>(null)
    val selectedMealDetails: StateFlow<MealDto?> = _selectedMealDetails

    // RecipeEntity used for detail screen and favorite toggle
    private val _selectedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val selectedRecipe: StateFlow<RecipeEntity?> = _selectedRecipe.asStateFlow()

    // Triggers navigation to detail screen
    private val _navigateToDetails = MutableStateFlow<RecipeEntity?>(null)
    val navigateToDetails: StateFlow<RecipeEntity?> = _navigateToDetails

    // Raw selected MealDto (used when coming from HomeScreen cards)
    private val _selectedMeal = MutableStateFlow<MealDto?>(null)
    val selectedMeal: StateFlow<MealDto?> = _selectedMeal

    // ─────────────────────────────────────────────
    // LOADING / ERROR / SNACKBAR STATE
    // ─────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    // ─────────────────────────────────────────────
    // CATEGORY STATE
    // ─────────────────────────────────────────────

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _generatedResults = MutableStateFlow<List<MealDto>>(emptyList())
    val generatedResults: StateFlow<List<MealDto>> = _generatedResults

    val generatedWithFavorites: StateFlow<List<MealDto>> = _generatedResults
        .combine(favoriteIds) { remoteMeals, cloudIds ->
            remoteMeals.map { it.copy(isFavorite = cloudIds.contains(it.idMeal)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─────────────────────────────────────────────
    // INIT — load default recipes on startup
    // ─────────────────────────────────────────────

    init {
        searchRecipes("s")
        fetchFirestoreFavorites()
    }

    private fun fetchFirestoreFavorites() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val cloudIds = snapshot?.get("favorites") as? List<String> ?: emptyList()
                _favoriteIds.value = cloudIds

                viewModelScope.launch {
                    val localFavorites = repository.favoriteRecipes.first()
                    val toDelete = localFavorites.filter { !cloudIds.contains(it.remoteId) }
                    toDelete.forEach { repository.delete(it) }

                    val localIds = localFavorites.map { it.remoteId }
                    val missingIds = cloudIds.filter { !localIds.contains(it) }

                    missingIds.forEach { id ->
                        try {
                            val response = repository.getRecipeById(id)
                            response.meals?.firstOrNull()?.let {
                                repository.insert(it.toEntity().copy(isFavorite = true))
                            }
                        } catch (e: Exception) { }
                    }
                }
            }
    }

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    // ─────────────────────────────────────────────
    // LOCAL ROOM CRUD
    // ─────────────────────────────────────────────

    fun insert(recipe: RecipeEntity) {
        viewModelScope.launch { repository.insert(recipe) }
    }

    fun update(recipe: RecipeEntity) {
        viewModelScope.launch { repository.update(recipe) }
    }

    fun delete(recipe: RecipeEntity) {
        viewModelScope.launch { repository.delete(recipe) }
    }

    /**
     * Toggle favorite — checks if already saved in Room.
     * If yes → removes from favorites.
     * If no → saves to Room as favorite.
     * Emits a snackbar message either way.
     */
    fun toggleFavorite(recipeId: String, isNowFavorite: Boolean, recipeEntity: RecipeEntity? = null) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        viewModelScope.launch {
            try {
                if (isNowFavorite) {
                    userRef.update(
                        "favorites",
                        com.google.firebase.firestore.FieldValue.arrayUnion(recipeId)
                    )
                    recipeEntity?.let { repository.insert(it.copy(isFavorite = true)) }
                    _snackbarMessage.emit("Added to Favorites")
                } else {
                    userRef.update(
                        "favorites",
                        com.google.firebase.firestore.FieldValue.arrayRemove(recipeId)
                    )
                    repository.deleteByRemoteId(recipeId)
                    _snackbarMessage.emit("Removed from Favorites")
                }
            } catch (e: Exception){
                _errorMessage.value = "Sync failed: ${e.message}"
            }
        }
    }

    // ─────────────────────────────────────────────
    // REMOTE API CALLS
    // ─────────────────────────────────────────────

    /**
     * Search recipes by name.
     * Default "s" on init loads a broad set of results.
     */
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

    /**
     * Filter by single ingredient — used by HomeScreen generate button.
     * MealDB free tier supports one ingredient at a time.
     */
    fun filterByIngredient(ingredient: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.filterByIngredient(ingredient)
                _generatedResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filter by category — used by RecipeGenerationScreen category chips.
     */
    fun filterByCategory(categoryName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedCategory.value = categoryName
            try {
                val response = repository.filterByCategory(categoryName)
                _searchResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load $categoryName recipes: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Select a meal by ID.
     * First checks Room for a local copy, falls back to API if not found.
     * Sets _selectedRecipe and _navigateToDetails to trigger navigation.
     */
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

    /**
     * Fetch full MealDto details separately (used when you need
     * the raw API data like instructions, YouTube link, etc.)
     */
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

    // ─────────────────────────────────────────────
    // CLEAR / RESET FUNCTIONS
    // ─────────────────────────────────────────────

    /**
     * Clear search results — used by the Clear button on HomeScreen
     */
    fun clearGeneratedResults() {
        _generatedResults.value = emptyList()
    }

    /**
     * Clear error message — used by Dismiss button on error state
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear selected meal — used when navigating back from detail screen
     */
    fun clearSelectedMeal() {
        _selectedMeal.value = null
        _selectedRecipe.value = null
    }

    /**
     * Called after navigating to detail screen to reset navigation trigger
     */
    fun onNavigated() {
        _navigateToDetails.value = null
        _selectedRecipe.value = null
    }

    /**
     * Reset everything back to the default broad search
     */
    fun resetToDefault() {
        _selectedCategory.value = null
        searchRecipes("s")
    }
}

// ─────────────────────────────────────────────
// MealDto → RecipeEntity converter
// Maps all 20 ingredient + measure pairs from the API
// into a single formatted string stored in Room
// ─────────────────────────────────────────────

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