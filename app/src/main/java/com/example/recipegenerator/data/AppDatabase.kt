package com.example.recipegenerator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recipegenerator.data.dao.IngredientDao
import com.example.recipegenerator.data.dao.RecipeDao
import com.example.recipegenerator.data.entity.IngredientEntity
import com.example.recipegenerator.data.entity.RecipeEntity

@Database(
    entities = [IngredientEntity::class, RecipeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_generator_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}