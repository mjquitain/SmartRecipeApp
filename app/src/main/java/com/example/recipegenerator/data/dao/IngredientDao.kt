package com.example.recipegenerator.data.dao

import androidx.room.*
import com.example.recipegenerator.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE name = :name AND userId = :userId")
    suspend fun getIngredientByName(name: String, userId: String): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE name = :name AND userId = :userId AND expirationDate = :expirationDate")
    suspend fun getIngredientByBatch(name: String, userId: String, expirationDate: String): IngredientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Query("SELECT * FROM ingredients WHERE userId = :userId ORDER BY name ASC")
    fun getIngredientsByUser(userId: String): Flow<List<IngredientEntity>>

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()
}