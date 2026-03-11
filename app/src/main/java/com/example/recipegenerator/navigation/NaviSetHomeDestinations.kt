package com.example.recipegenerator.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.*
import com.example.recipegenerator.ui.screens.HomeScreen
import com.example.recipegenerator.ui.screens.IngredientsListScreen
import com.example.recipegenerator.ui.screens.RecipeDetailScreen
import com.example.recipegenerator.ui.screens.RecipeGenerationScreen
import com.example.recipegenerator.ui.viewmodel.IngredientViewModel
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.toRoute
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect

fun NavGraphBuilder.naviSetHomeDestinations(
    paddingValues: PaddingValues = PaddingValues(),
    upperNavController: NavController,
    ingredientViewModel: IngredientViewModel,
    recipeViewModel: RecipeViewModel
) {
    composable<LandingGraph> {
        val localNavController = rememberNavController()

        Scaffold(
            bottomBar = { CircularBottomNavigationBar(localNavController) }
        ) { paddingValues ->
            NavHost(
                startDestination = LandingGraph.HomeNode,
                route = LandingGraph::class,
                navController = localNavController
            ) {
                composable<LandingGraph.HomeNode> {
                    HomeScreen(
                        padding = paddingValues,
                        recipeViewModel = recipeViewModel,
                        onProfileClick = {
                            upperNavController.navigate(SettingsGraph)
                        },
                        onNavigateToRecipes = {
                            localNavController.navigate(LandingGraph.RecipesNode)
                        },

                        onRecipeClick = { meal ->
                            localNavController.navigate(
                                LandingGraph.RecipeDetailNode(recipeId = meal.idMeal)
                            )
                        }
                    )
                }

                composable<LandingGraph.IngredientsNode> {
                    IngredientsListScreen(
                        padding = paddingValues,
                        ingredientViewModel = ingredientViewModel,
                        onProfileClick = {
                            upperNavController.navigate(SettingsGraph)
                        }
                    )
                }

                composable<LandingGraph.RecipesNode> {
                    RecipeGenerationScreen(
                        padding = paddingValues,
                        recipeViewModel = recipeViewModel,
                        onProfileClick = {
                            upperNavController.navigate(SettingsGraph)
                        },
                        onRecipeClick = { recipe ->
                            // RecipeGenerationScreen still uses navigateToDetails normally
                            localNavController.navigate(
                                LandingGraph.RecipeDetailNode(recipeId = recipe.remoteId ?: recipe.id.toString())
                            )
                        },
                        onNavigateToHome = {
                            localNavController.navigate(LandingGraph.HomeNode)
                        }
                    )
                }

                composable<LandingGraph.RecipeDetailNode> { backStackEntry ->
                    val recipeId = backStackEntry.toRoute<LandingGraph.RecipeDetailNode>().recipeId
                    val selectedRecipe by recipeViewModel.selectedRecipe.collectAsState()

                    // Fetch details when this screen first appears
                    LaunchedEffect(recipeId) {
                        recipeViewModel.selectMeal(recipeId)
                    }

                    if (selectedRecipe != null) {
                        RecipeDetailScreen(
                            recipe = selectedRecipe!!,
                            onBackClick = {
                                recipeViewModel.onNavigated()
                                localNavController.popBackStack()
                            },
                            onFavoriteClick = {
                                recipeViewModel.toggleFavorite(selectedRecipe!!)
                            }
                        )
                    } else {
                        // Loading state while fetching details
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularBottomNavigationBar(navController: NavController?) {
    Box(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f).height(70.dp).align(Alignment.Center),
            color = Color.White,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(50.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(
                    icon = Icons.Default.Email,
                    label = "Recipes",
                    isSelected = navController?.currentDestination?.hasRoute(LandingGraph.RecipesNode::class) == true,
                    onClick = { navController?.navigate(LandingGraph.RecipesNode) }
                )
                NavigationItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = navController?.currentDestination?.hasRoute(LandingGraph.HomeNode::class) == true,
                    onClick = { navController?.navigate(LandingGraph.HomeNode) }
                )
                NavigationItem(
                    icon = Icons.Default.List,
                    label = "Ingredients",
                    isSelected = navController?.currentDestination?.hasRoute(LandingGraph.IngredientsNode::class) == true,
                    onClick = { navController?.navigate(LandingGraph.IngredientsNode) }
                )
            }
        }
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}