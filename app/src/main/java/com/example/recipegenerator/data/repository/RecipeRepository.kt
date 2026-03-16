package com.example.recipegenerator.data.repository

import com.example.recipegenerator.data.dao.RecipeDao
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.network.MealApiService
import com.example.recipegenerator.network.MealResponse
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
        val existingRecipe = recipeDao.getRecipeByRemoteId(recipe.remoteId)
        if (existingRecipe != null) {
            recipeDao.deleteRecipe(existingRecipe)
        } else {
            recipeDao.insertRecipe(recipe.copy(isFavorite = true))
        }
    }

    suspend fun getLocalRecipeById(remoteId: String): RecipeEntity? {
        return recipeDao.getRecipeByRemoteId(remoteId)
    }

    suspend fun getLocalRecipeByRemoteId(remoteId: String): RecipeEntity? {
        return recipeDao.getRecipeByRemoteId(remoteId)
    }

    // REMOTE - MealDB API
    suspend fun searchRecipesByName(query: String) = apiService.searchMealsByName(query)

    suspend fun getRecipeById(id: String) = apiService.getMealById(id)

    suspend fun filterByCategory(category: String): MealResponse {
        return apiService.filterByCategory(category)
        }

    suspend fun filterByIngredient(ingredient: String) = apiService.filterByIngredient(ingredient)

    suspend fun getMealDetails(id: String) = apiService.getMealById(id)

    suspend fun deleteByRemoteId(remoteId: String) {
        recipeDao.deleteByRemoteId(remoteId)
    }
}
