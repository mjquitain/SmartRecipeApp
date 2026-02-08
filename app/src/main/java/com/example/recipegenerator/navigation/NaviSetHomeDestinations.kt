package com.example.recipegenerator.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipegenerator.ui.screens.HomeScreen
import com.example.recipegenerator.ui.screens.IngredientsListScreen
import com.example.recipegenerator.ui.screens.RecipeGenerationScreen
import kotlin.reflect.KClass

fun NavGraphBuilder.naviSetHomeDestinations(paddingValues : PaddingValues = PaddingValues(), upperNavController : NavController) {
    composable<LandingGraph> {
        // Local navigation controller used to allow this composable to both
        // handle home screens navigation AND display + control the navbar.
        val localNavController = rememberNavController()

        Scaffold(
            bottomBar = { CircularBottomNavigationBar(localNavController) }
        ) {
            NavHost(
                startDestination = LandingGraph.HomeNode,
                route = LandingGraph::class,
                navController = localNavController
            ) {
                composable<LandingGraph.HomeNode> {
                    HomeScreen(
                        padding = paddingValues,
                        onProfileClick = {
                            upperNavController.navigate(SettingsGraph)
                        },
                        onNavigateToRecipes = {
                            localNavController.navigate(LandingGraph.RecipesNode)
                        }
                    )
                }

                composable<LandingGraph.IngredientsNode> {
                    val composeAndroidContext = LocalContext.current

                    IngredientsListScreen(
                        padding = paddingValues,
                        onAddClick = {
                            // TODO: Insert add ingredient logic here.
                            Toast.makeText(
                                composeAndroidContext,
                                "TODO: Insert ingredient details here\nResponse test for add ingredient",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    )
                }

                composable<LandingGraph.RecipesNode> {
                    val composeAndroidContext = LocalContext.current

                    RecipeGenerationScreen(
                        padding = paddingValues,
                        onProfileClick = {

                        },
                        onRecipeClick = { recipe ->
                            // TODO: Insert recipe details here
                            //println("Navigate to recipe details: ${recipe.name}")
                            Toast.makeText(
                                composeAndroidContext,
                                "TODO: Insert recipe details here\nNavigate to recipe details: ${recipe.name}",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onNavigateToHome = {
                            localNavController.navigate(LandingGraph.HomeNode)
                        }
                    )
                }
            }
        }
    }
}



// TODO: There's an official bottom bar (NavigationBar): https://developer.android.com/develop/ui/compose/components/navigation-bar
@Composable
fun CircularBottomNavigationBar(
    navController: NavController?
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        // Oval container
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(70.dp)
                .align(Alignment.Center),
            color = Color.White,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left - Recipes
                NavigationItem(
                    icon = Icons.Default.Email,
                    label = "Recipes",
                    isSelected = navController?.currentDestination?.hasRoute(LandingGraph.RecipesNode::class) == true,
                    onClick = { navController?.navigate(LandingGraph.RecipesNode) }
                )

                // Center - Home
                NavigationItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = navController?.currentDestination?.hasRoute(LandingGraph.HomeNode::class) == true,
                    onClick = { navController?.navigate(LandingGraph.HomeNode) }
                )

                // Right - Ingredients
                NavigationItem(
                    icon = Icons.Default.List,
                    label = "Ingredients",
                    isSelected =  navController?.currentDestination?.hasRoute(LandingGraph.IngredientsNode::class) == true,
                    onClick = { navController?.navigate(LandingGraph.IngredientsNode) }
                )
            }
        }
    }
}



// TODO: There's an official navigation item (NavigationBarItem): https://developer.android.com/develop/ui/compose/components/navigation-bar
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
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
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