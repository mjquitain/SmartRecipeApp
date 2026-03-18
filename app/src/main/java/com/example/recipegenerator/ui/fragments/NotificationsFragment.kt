package com.example.recipegenerator.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.ui.screens.NotificationsScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class NotificationsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity() as HomeActivity
        val userId = requireActivity()
            .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("current_username", "") ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel, appSettingsViewModel = activity.appSettingsViewModel) {
                    NotificationsScreen(
                        notificationViewModel = activity.notificationViewModel,
                        userId = userId,
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}