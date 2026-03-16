package com.example.recipegenerator.data.repository

import android.util.Log
import com.example.recipegenerator.data.dao.IngredientDao
import com.example.recipegenerator.data.entity.IngredientEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class IngredientRepository(
    private val ingredientDao: IngredientDao,
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    val allIngredients: Flow<List<IngredientEntity>> = ingredientDao.getAllIngredients()

    suspend fun addIngredient(ingredient: IngredientEntity) {
        ingredientDao.insertIngredient(ingredient)
        syncToFirestore(ingredient)
    }

    suspend fun update(ingredient: IngredientEntity) {
        ingredientDao.updateIngredient(ingredient)
        syncToFirestore(ingredient)

    }

    suspend fun delete(ingredient: IngredientEntity) {
        ingredientDao.deleteIngredient(ingredient)

        try {
            val documentId = "${ingredient.name}_${ingredient.expirationDate}"

            firestore.collection("users")
                .document(ingredient.userId)
                .collection("ingredients")
                .document(documentId)
                .delete()
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getByName(name: String, userId: String): IngredientEntity? {
        return ingredientDao.getIngredientByName(name, userId)
    }

    private suspend fun syncToFirestore(ingredient: IngredientEntity) {
        try {
            val ingredientMap = hashMapOf(
                "name" to ingredient.name,
                "category" to ingredient.category,
                "quantity" to ingredient.quantity,
                "unit" to ingredient.unit,
                "expirationDate" to ingredient.expirationDate,
                "updatedAt" to System.currentTimeMillis(),
                "userId" to ingredient.userId
            )

            val documentId = "${ingredient.name}_${ingredient.expirationDate}"

            firestore.collection("users")
                .document(ingredient.userId)
                .collection("ingredients")
                .document(documentId)
                .set(ingredientMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
        }
    }
}