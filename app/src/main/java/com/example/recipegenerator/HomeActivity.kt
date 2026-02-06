package com.example.recipegenerator

import android.content.Context
import android.content.Intent
import android.app.Activity
import com.example.recipegenerator.SplashActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipegenerator.navigation.LandingGraph
import com.example.recipegenerator.navigation._ROOTGRAPH
import com.example.recipegenerator.navigation.naviSetHomeDestinations
import com.example.recipegenerator.navigation.naviSetSettingsDestinations
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeGeneratorTheme {
                val context = LocalContext.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val rootNavigationNode = rememberNavController()

                    NavHost(
                        route = _ROOTGRAPH::class,
                        startDestination = LandingGraph,
                        navController = rootNavigationNode,
                    ) {
                        naviSetHomeDestinations(upperNavController = rootNavigationNode)
                        naviSetSettingsDestinations(
                            navigationNode = rootNavigationNode,
                            onLogOut = {
                                val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().clear().apply()
//                                val editor = sharedPref.edit()

                                val intent = Intent(context, SplashActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            }
                        )
                    }
                }
            }
        }
    }
}



// NOTE: Moved AppNavigation to its own file for encapsulation.
// For AppNavigation and its components, see recipegenerator/navigation/NaviSetHomeDestinations.kt
//
// For SettingsNavigation, see recipegenerator/navigation/NaviSetSettingsDestinations.kt
//@Composable
//fun AppNavigation(
//    navigationNode : NavHostController
//) {
//    val composeAndroidContext = LocalContext.current
//
//
//
//    Scaffold(
//        bottomBar = {
//            CircularBottomNavigationBar(
//                currentDestination = navigationNode.currentDestination,
//                onNavigate = {
//                    screen -> navigationNode.navigate(screen)
//                }
//            )
//        }
//    ) {
//        paddingValues -> NavHost(
//            navController = navigationNode,
//            route = LandingGraph::class,
//            startDestination = LandingGraph.HomeNode,
//        ) {
//            composable<LandingGraph.HomeNode> { HomeScreen(
//                padding = paddingValues,
//                onProfileClick = {
//                    navigationNode.navigate(SettingsGraph)
//                },
//                onNavigateToRecipes = {
//                    navigationNode.navigate(LandingGraph.RecipesNode)
//                }
//            ) }
//
//            composable<LandingGraph.IngredientsNode> { IngredientsListScreen(
//                padding = paddingValues,
//                onAddClick = {
//                    // TODO: Insert add ingredient logic here.
//                    Toast.makeText(
//                        composeAndroidContext,
//                        "TODO: Insert recipe details here\n\nResponse test for add ingredient",
//                        4
//                    )
//                }
//            ) }
//
//            composable<LandingGraph.RecipesNode> { RecipeGenerationScreen(
//                padding = paddingValues,
//                onProfileClick = {
//
//                },
//                onRecipeClick = { recipe ->
//                    // TODO: Insert recipe details here
//                    //println("Navigate to recipe details: ${recipe.name}")
//                    Toast.makeText(
//                        composeAndroidContext,
//                        "TODO: Insert recipe details here\n\nNavigate to recipe details: ${recipe.name}",
//                        5
//                    )
//                },
//                onNavigateToHome = {
//                    navigationNode.navigate(LandingGraph.HomeNode)
//                }
//            ) }
//        }
//    }
//}