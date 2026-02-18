package com.example.recipegenerator

import android.app.Application
import com.example.recipegenerator.data.AppDatabase
import com.example.recipegenerator.data.repository.IngredientRepository
import com.example.recipegenerator.data.repository.RecipeRepository
import com.example.recipegenerator.network.RetrofitInstance

class RecipeApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val ingredientRepository by lazy {
        IngredientRepository(database.ingredientDao())
    }

    val recipeRepository by lazy {
        RecipeRepository(database.recipeDao(), RetrofitInstance.api)
    }
}