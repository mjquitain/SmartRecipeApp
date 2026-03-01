package com.example.recipegenerator.navigation

import kotlinx.serialization.Serializable

@Serializable object _ROOTGRAPH

@Serializable object LandingGraph {
    @Serializable object HomeNode
    @Serializable object IngredientsNode
    @Serializable object RecipesNode
    @Serializable data class RecipeDetailNode(val recipeId: String)
}

@Serializable object SettingsGraph {
    @Serializable object ProfileNode
    @Serializable object NotificationsNode
}