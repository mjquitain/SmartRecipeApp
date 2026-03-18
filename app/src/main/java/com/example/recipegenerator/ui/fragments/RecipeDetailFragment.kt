package com.example.recipegenerator.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.ui.screens.RecipeDetailScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class RecipeDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val recipeId = arguments?.getString("recipeId") ?: ""
        val activity = requireActivity() as HomeActivity

        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel, appSettingsViewModel = activity.appSettingsViewModel) {
                    val selectedRecipe by activity.recipeViewModel.selectedRecipe.collectAsState()
                    val favoriteIds by activity.recipeViewModel.favoriteIds.collectAsState()
                    val isFavorited = favoriteIds.contains(recipeId)

                    LaunchedEffect(recipeId) {
                        activity.recipeViewModel.selectMeal(recipeId)
                    }

                    if (selectedRecipe != null) {
                        RecipeDetailScreen(
                            recipe = selectedRecipe!!.copy(isFavorite = isFavorited),
                            onBackClick = {
                                activity.recipeViewModel.onNavigated()
                                findNavController().popBackStack()
                            },
                            onFavoriteClick = {
                                activity.recipeViewModel.toggleFavorite(
                                    recipeId = recipeId,
                                    isNowFavorite = !isFavorited,
                                    recipeEntity = selectedRecipe
                                )
                            },
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}