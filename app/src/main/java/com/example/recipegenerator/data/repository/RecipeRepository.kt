package com.example.recipegenerator.data.repository

import com.example.recipegenerator.data.dao.RecipeDao
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.network.MealApiService
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val apiService: MealApiService
) {

    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()
    val favoriteRecipes: Flow<List<RecipeEntity>> = recipeDao.getFavoriteRecipes()

    // LOCAL CRUD
    suspend fun insert(recipe: RecipeEntity) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun update(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun delete(recipe: RecipeEntity) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun toggleFavorite(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
    }

    // REMOTE - MealDB API
    suspend fun searchRecipesByName(name: String) = apiService.searchMealsByName(name)

    suspend fun getRecipeById(id: String) = apiService.getMealById(id)

    suspend fun getRandomRecipe() = apiService.getRandomMeal()

    suspend fun filterByCategory(category: String) = apiService.filterByCategory(category)

    suspend fun filterByIngredient(ingredient: String) = apiService.filterByIngredient(ingredient)
}