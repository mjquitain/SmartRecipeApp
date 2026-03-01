package com.example.recipegenerator.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recipes", indices = [Index(value = ["remoteId"], unique = true)])
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String,
    val name: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val area: String = "",
    val ingredients: String = "",
    val instruction: String = "",
    val isFavorite: Boolean = false
)