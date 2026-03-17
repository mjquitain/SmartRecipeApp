package com.example.recipegenerator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients", primaryKeys = ["userId", "name", "expirationDate"])
data class IngredientEntity(
    val userId: String,
    val name: String = "",
    val category: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val expirationDate: String,
    val updatedAt: Long = System.currentTimeMillis()
)