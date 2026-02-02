package com.example.recipegenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.ui.screens.HomeScreen
import com.example.recipegenerator.ui.screens.IngredientsListScreen
import com.example.recipegenerator.ui.screens.RecipeGenerationScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

/**
 * HomeActivity - Contains your Compose screens
 * Navigated to from MainActivity after login
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // TODO: No time to apply this yet, but Compose's navigation
    //   dependencies are now installed. Change how the app
    //   navigates (i.e. replace these strings and the goto function
    //   with a NavGraph and NavController.
    var currentScreen by remember { mutableStateOf("home") }
    var lastScreen by remember { mutableStateOf("home") }


    fun goto(screen : String) {
        if (screen == currentScreen) {
            return
        }

        lastScreen = currentScreen
        currentScreen = screen
    }



    Scaffold(
        bottomBar = {
            CircularBottomNavigationBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> goto(screen) }
            )
        }
    ) { paddingValues ->
        when (currentScreen) {
            "ingredients" -> {
                IngredientsListScreen(
                    padding = paddingValues,
                    onAddClick = {
                        println("Response test for add ingredient")
                    }
                )
            }
            "home" -> {
                // HomeScreen = "Want to look for a meal?" page


                HomeScreen(
                    padding = paddingValues,
                    onProfileClick = {
                        goto("profile")
                    },
                    onNavigateToRecipes = {
                        goto("recipes")
                    }
                )
            }
            "recipes" -> {
                // RecipeGenerationScreen = Available/Favorite Recipes
                RecipeGenerationScreen(
                    padding = paddingValues,
                    onProfileClick = {
                        goto("profile")
                    },
                    onRecipeClick = { recipe ->
                        // TODO: Insert recipe details here
                        println("Navigate to recipe details: ${recipe.name}")
                    },
                    onNavigateToHome = {
                        goto("home")
                    }
                )
            }
            "profile" -> {
                // ProfileScreen
                ProfileScreen(
                    padding = paddingValues,
                    onBackClick = {goto(lastScreen)}
                )
            }
            
        }
    }
}

@Composable
fun CircularBottomNavigationBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
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
                    isSelected = currentScreen == "recipes",
                    onClick = { onNavigate("recipes") }
                )

                // Center - Home
                NavigationItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentScreen == "home",
                    onClick = { onNavigate("home") }
                )

                // Right - Ingredients
                NavigationItem(
                    icon = Icons.Default.List,
                    label = "Ingredients",
                    isSelected = currentScreen == "ingredients",
                    onClick = { onNavigate("ingredients") }
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