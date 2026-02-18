package com.example.recipegenerator.model

data class Ingredient(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val quantity: String = "",
    val unit: String = "",
    val expirationDate: String = "" // Format: "yyyy-MM-dd"
)