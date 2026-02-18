package com.example.recipegenerator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",   // stored as comma-separated string
    val cookingTime: Int = 0,
    val difficulty: String = "Easy",
    val isFavorite: Boolean = false
)