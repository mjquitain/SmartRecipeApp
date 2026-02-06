package com.example.recipegenerator.navigation

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.recipegenerator.ui.screens.NotificationsScreen
import com.example.recipegenerator.ui.screens.ProfileScreen


fun NavGraphBuilder.naviSetSettingsDestinations(navigationNode : NavController, onLogOut: () -> Unit) {
    navigation(
        startDestination = SettingsGraph.ProfileNode,
        route = SettingsGraph::class
    ) {
        composable<SettingsGraph.ProfileNode> { ProfileScreen(
            onBackClick = { navigationNode.popBackStack() },
            navController = navigationNode,
            onLogOutClick = onLogOut
        ) }

        composable<SettingsGraph.NotificationsNode> { NotificationsScreen(
            onBackClick = { navigationNode.popBackStack() }
        ) }
    }
}