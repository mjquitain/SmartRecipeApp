package com.example.recipegenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.data.repository.RecipeRepository
import com.example.recipegenerator.network.MealDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val db   = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    // ── Favorites ─────────────────────────────────────────────────────────────
    private val _favoriteIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteIds: StateFlow<List<String>> = _favoriteIds.asStateFlow()

    val favoriteRecipes: StateFlow<List<RecipeEntity>> = repository.favoriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Search / API ──────────────────────────────────────────────────────────
    private val _searchResults = MutableStateFlow<List<MealDto>>(emptyList())
    val searchResults: StateFlow<List<MealDto>> = _searchResults

    val searchResultsWithFavorites: StateFlow<List<MealDto>> = _searchResults
        .combine(favoriteIds) { remoteMeals, cloudIds ->
            remoteMeals.map { it.copy(isFavorite = cloudIds.contains(it.idMeal)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Generated (Home screen) ───────────────────────────────────────────────
    private val _generatedResults = MutableStateFlow<List<MealDto>>(emptyList())
    val generatedResults: StateFlow<List<MealDto>> = _generatedResults

    val generatedWithFavorites: StateFlow<List<MealDto>> = _generatedResults
        .combine(favoriteIds) { remoteMeals, cloudIds ->
            remoteMeals.map { it.copy(isFavorite = cloudIds.contains(it.idMeal)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Detail / Navigation ───────────────────────────────────────────────────
    private val _selectedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val selectedRecipe: StateFlow<RecipeEntity?> = _selectedRecipe.asStateFlow()

    private val _navigateToDetails = MutableStateFlow<RecipeEntity?>(null)
    val navigateToDetails: StateFlow<RecipeEntity?> = _navigateToDetails

    private val _selectedMealDetails = MutableStateFlow<MealDto?>(null)
    val selectedMealDetails: StateFlow<MealDto?> = _selectedMealDetails

    private val _selectedMeal = MutableStateFlow<MealDto?>(null)
    val selectedMeal: StateFlow<MealDto?> = _selectedMeal

    // ── Loading / Error / Snackbar ────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    // ── Category ──────────────────────────────────────────────────────────────
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // ── Tab (persists across back navigation) ─────────────────────────────────
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun setTab(index: Int) { _selectedTab.value = index }

    // ── Selected Ingredients (persists across back navigation) ────────────────
    private val _selectedIngredients = MutableStateFlow<Set<String>>(emptySet())
    val selectedIngredients: StateFlow<Set<String>> = _selectedIngredients.asStateFlow()

    fun addIngredient(ingredient: String) {
        _selectedIngredients.value = _selectedIngredients.value + ingredient
    }

    fun removeIngredient(ingredient: String) {
        _selectedIngredients.value = _selectedIngredients.value - ingredient
    }

    fun clearIngredients() {
        _selectedIngredients.value = emptySet()
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        searchRecipes("chicken") // broad default that returns real results
        fetchFirestoreFavorites()
    }

    // ── Firestore ─────────────────────────────────────────────────────────────
    private fun fetchFirestoreFavorites() {
        try {
            val uid = auth.currentUser?.uid ?: return
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    val cloudIds = snapshot?.get("favorites") as? List<String> ?: emptyList()
                    _favoriteIds.value = cloudIds

                    viewModelScope.launch {
                        try {
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
                        } catch (e: Exception) { }
                    }
                }
        } catch (e: Exception) { }
    }

    // ── Room CRUD ─────────────────────────────────────────────────────────────
    fun insert(recipe: RecipeEntity) { viewModelScope.launch { repository.insert(recipe) } }
    fun update(recipe: RecipeEntity) { viewModelScope.launch { repository.update(recipe) } }
    fun delete(recipe: RecipeEntity) { viewModelScope.launch { repository.delete(recipe) } }

    // ── Toggle Favorite ───────────────────────────────────────────────────────
    fun toggleFavorite(
        recipeId: String,
        isNowFavorite: Boolean,
        recipeEntity: RecipeEntity? = null
    ) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        viewModelScope.launch {
            try {
                if (isNowFavorite) {
                    userRef.update(
                        "favorites",
                        com.google.firebase.firestore.FieldValue.arrayUnion(recipeId)
                    )
                    // Fetch full details if instruction is missing
                    val entityToSave = if (
                        recipeEntity?.instruction.isNullOrBlank() ||
                        recipeEntity?.instruction == "No instructions available"
                    ) {
                        try {
                            val response = repository.getRecipeById(recipeId)
                            response.meals?.firstOrNull()?.toEntity()?.copy(isFavorite = true)
                                ?: recipeEntity?.copy(isFavorite = true)
                        } catch (e: Exception) {
                            recipeEntity?.copy(isFavorite = true)
                        }
                    } else {
                        recipeEntity?.copy(isFavorite = true)
                    }
                    entityToSave?.let { repository.insert(it) }
                    _snackbarMessage.emit("Added to Favorites")

                } else {
                    userRef.update(
                        "favorites",
                        com.google.firebase.firestore.FieldValue.arrayRemove(recipeId)
                    )
                    repository.deleteByRemoteId(recipeId)
                    _snackbarMessage.emit("Removed from Favorites")
                }
            } catch (e: Exception) {
                // Firebase failed — fall back to Room only
                if (isNowFavorite) {
                    recipeEntity?.copy(isFavorite = true)?.let { repository.insert(it) }
                    _snackbarMessage.emit("Added to Favorites")
                } else {
                    repository.deleteByRemoteId(recipeId)
                    _snackbarMessage.emit("Removed from Favorites")
                }
            }
        }
    }

    // ── API Calls ─────────────────────────────────────────────────────────────
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            _selectedCategory.value = null  // clear category on any search
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

    fun filterByIngredient(ingredient: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.filterByIngredient(ingredient)
                _generatedResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    // ── Clear / Reset ─────────────────────────────────────────────────────────
    fun clearGeneratedResults() { _generatedResults.value = emptyList() }
    fun clearError()            { _errorMessage.value = null }
    fun clearSelectedMeal()     { _selectedMeal.value = null; _selectedRecipe.value = null }
    fun onNavigated()           { _navigateToDetails.value = null; _selectedRecipe.value = null }

    fun resetToDefault() {
        _selectedCategory.value = null
        _searchResults.value = emptyList()
    }
}

// ── MealDto → RecipeEntity ────────────────────────────────────────────────────
fun MealDto.toEntity(): RecipeEntity {
    fun pair(ing: String?, meas: String?): String? =
        if (!ing.isNullOrBlank()) { if (!meas.isNullOrBlank()) "$meas $ing" else ing } else null

    val ingredientList = listOfNotNull(
        pair(strIngredient1, strMeasure1),  pair(strIngredient2, strMeasure2),
        pair(strIngredient3, strMeasure3),  pair(strIngredient4, strMeasure4),
        pair(strIngredient5, strMeasure5),  pair(strIngredient6, strMeasure6),
        pair(strIngredient7, strMeasure7),  pair(strIngredient8, strMeasure8),
        pair(strIngredient9, strMeasure9),  pair(strIngredient10, strMeasure10),
        pair(strIngredient11, strMeasure11), pair(strIngredient12, strMeasure12),
        pair(strIngredient13, strMeasure13), pair(strIngredient14, strMeasure14),
        pair(strIngredient15, strMeasure15), pair(strIngredient16, strMeasure16),
        pair(strIngredient17, strMeasure17), pair(strIngredient18, strMeasure18),
        pair(strIngredient19, strMeasure19), pair(strIngredient20, strMeasure20)
    )

    return RecipeEntity(
        remoteId    = this.idMeal,
        name        = this.strMeal,
        imageUrl    = this.strMealThumb ?: "",
        category    = this.strCategory ?: "",
        area        = this.strArea ?: "",
        ingredients = ingredientList.joinToString("\n● ", "● "),
        instruction = this.strInstructions ?: "No instructions available",
        isFavorite  = false
    )
}

// ── Factory ───────────────────────────────────────────────────────────────────
class RecipeViewModelFactory(
    private val repository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}