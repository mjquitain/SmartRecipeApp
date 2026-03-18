package com.example.recipegenerator.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.R
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.ui.screens.HomeScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity() as HomeActivity
        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel, appSettingsViewModel = activity.appSettingsViewModel) {
                    HomeScreen(
                        recipeViewModel = activity.recipeViewModel,
                        ingredientViewModel = activity.ingredientViewModel,
                        onProfileClick = {
                            findNavController().navigate(R.id.action_home_to_profile)
                        },
                        onRecipeClick = { meal ->
                            activity.recipeViewModel.selectMeal(meal.idMeal)
                            val bundle = android.os.Bundle().apply {
                                putString("recipeId", meal.idMeal)
                            }
                            findNavController().navigate(R.id.action_home_to_detail, bundle)
                        }
                    )
                }
            }
        }
    }
}