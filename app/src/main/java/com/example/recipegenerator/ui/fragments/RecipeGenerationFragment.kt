package com.example.recipegenerator.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.R
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.ui.screens.RecipeGenerationScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class RecipeGenerationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity() as HomeActivity
        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel, appSettingsViewModel = activity.appSettingsViewModel) {
                    RecipeGenerationScreen(
                        recipeViewModel = activity.recipeViewModel,
                        onProfileClick = {
                            findNavController().navigate(R.id.action_recipes_to_profile)
                        },
                        onRecipeClick = { recipe ->
                            val bundle = android.os.Bundle().apply {
                                putString("recipeId", recipe.remoteId ?: recipe.id.toString())
                            }
                            findNavController().navigate(R.id.action_recipes_to_detail, bundle)
                        }
                    )
                }
            }
        }
    }
}