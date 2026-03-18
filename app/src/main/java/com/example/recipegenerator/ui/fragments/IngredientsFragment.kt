package com.example.recipegenerator.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.R
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.ui.screens.IngredientsListScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class IngredientsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity() as HomeActivity
        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel, appSettingsViewModel = activity.appSettingsViewModel) {
                    IngredientsListScreen(
                        ingredientViewModel = activity.ingredientViewModel,
                        onProfileClick = {
                            findNavController().navigate(R.id.action_ingredients_to_profile)
                        }
                    )
                }
            }
        }
    }
}