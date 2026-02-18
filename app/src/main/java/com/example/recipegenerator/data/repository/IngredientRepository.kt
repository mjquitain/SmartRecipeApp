package com.example.recipegenerator.data.repository

import com.example.recipegenerator.data.dao.IngredientDao
import com.example.recipegenerator.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val ingredientDao: IngredientDao) {

    val allIngredients: Flow<List<IngredientEntity>> = ingredientDao.getAllIngredients()

    suspend fun insert(ingredient: IngredientEntity) {
        ingredientDao.insertIngredient(ingredient)
    }

    suspend fun update(ingredient: IngredientEntity) {
        ingredientDao.updateIngredient(ingredient)
    }

    suspend fun delete(ingredient: IngredientEntity) {
        ingredientDao.deleteIngredient(ingredient)
    }

    suspend fun getById(id: Int): IngredientEntity? {
        return ingredientDao.getIngredientById(id)
    }
}