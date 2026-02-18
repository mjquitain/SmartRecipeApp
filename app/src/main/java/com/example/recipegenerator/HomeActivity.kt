package com.example.recipegenerator

import android.content.Context
import android.content.Intent
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.recipegenerator.navigation.LandingGraph
import com.example.recipegenerator.navigation._ROOTGRAPH
import com.example.recipegenerator.navigation.naviSetHomeDestinations
import com.example.recipegenerator.navigation.naviSetSettingsDestinations
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme
import com.example.recipegenerator.ui.viewmodel.IngredientViewModel
import com.example.recipegenerator.ui.viewmodel.IngredientViewModelFactory
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel
import com.example.recipegenerator.ui.viewmodel.RecipeViewModelFactory

class HomeActivity : ComponentActivity() {

    private val ingredientViewModel: IngredientViewModel by viewModels {
        IngredientViewModelFactory((application as RecipeApp).ingredientRepository)
    }

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory((application as RecipeApp).recipeRepository)
    }

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
                        naviSetHomeDestinations(
                            upperNavController = rootNavigationNode,
                            ingredientViewModel = ingredientViewModel,
                            recipeViewModel = recipeViewModel
                        )
                        naviSetSettingsDestinations(
                            navigationNode = rootNavigationNode,
                            onLogOut = {
                                val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().clear().apply()
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