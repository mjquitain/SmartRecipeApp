package com.example.recipegenerator.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.recipegenerator.ui.screens.NotificationsScreen
import com.example.recipegenerator.ui.screens.ProfileScreen
import com.example.recipegenerator.ui.viewmodel.ProfileViewModel


fun NavGraphBuilder.naviSetSettingsDestinations(
    navigationNode: NavController,
    onLogOut: () -> Unit,
    profileViewModel: ProfileViewModel
) {
    navigation(
        startDestination = SettingsGraph.ProfileNode,
        route = SettingsGraph::class
    ) {
        composable<SettingsGraph.ProfileNode> { ProfileScreen(
            onBackClick = { navigationNode.popBackStack() },
            navController = navigationNode,
            onLogOutClick = onLogOut,
            profileViewModel = profileViewModel
        ) }

        composable<SettingsGraph.NotificationsNode> { NotificationsScreen(
            onBackClick = { navigationNode.popBackStack() }
        ) }
    }
}