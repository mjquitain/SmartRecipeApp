package com.example.recipegenerator.model

data class Recipe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val cookingTime: Int = 0,
    val difficulty: String = "Easy",
    val isFavorite: Boolean = false
)