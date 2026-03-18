package com.example.recipegenerator.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.ui.screens.AppSettingsScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class AppSettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity() as HomeActivity
        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel) {
                    AppSettingsScreen(
                        appSettingsViewModel = activity.appSettingsViewModel,
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}