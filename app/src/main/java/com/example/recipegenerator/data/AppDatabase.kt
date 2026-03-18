package com.example.recipegenerator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recipegenerator.data.dao.*
import com.example.recipegenerator.data.entity.*

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        UserEntity::class,
        AppSettingsEntity::class,
        NotificationEntity::class
    ],
    version = 5, // bump version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_generator_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}