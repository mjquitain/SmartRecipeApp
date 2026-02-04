package com.example.recipegenerator.navigation

import kotlinx.serialization.Serializable

@Serializable object _ROOTGRAPH

@Serializable object LandingGraph {
    @Serializable object HomeNode
    @Serializable object IngredientsNode
    @Serializable object RecipesNode
    // ... TODO: Add more nodes under HomeNode when creating new pages.
}

@Serializable object SettingsGraph {
    @Serializable object ProfileNode
    @Serializable object NotificationsNode
}