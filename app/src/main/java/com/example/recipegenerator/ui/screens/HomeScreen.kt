package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.network.MealDto
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel

/**
 * HomeScreen
 *
 * FLOW:
 * 1. User selects ingredients
 * 2. Clicks Generate → calls MealDB API via recipeViewModel.filterByIngredient()
 * 3. Results appear as cards BELOW on this same screen
 * 4. User taps a card → onRecipeClick(meal) → navigates to RecipeDetailNode
 *
 * onNavigateToRecipes is kept for the nav bar tab only, NOT for the generate button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues = PaddingValues(),
    recipeViewModel: RecipeViewModel,
    onProfileClick: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onRecipeClick: (MealDto) -> Unit = {}   // NEW — tapping a result card
) {
    var inputText by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }

    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()

    // API results — populated after Generate is clicked
    val searchResults by recipeViewModel.searchResults.collectAsState()

    val availableIngredients = listOf(
        "chicken", "egg", "spinach", "cucumber",
        "tomato", "onion", "garlic", "beef",
        "pork", "fish", "potato", "carrot"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Home", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, "Profile", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe Generation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Want to look for a meal?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Enter the ingredients and we'll suggest delicious recipes.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter ingredient...") },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (inputText.isNotBlank() && inputText.lowercase() !in selectedIngredients) {
                                    selectedIngredients = selectedIngredients + inputText.lowercase().trim()
                                    inputText = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, "Add ingredient")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (selectedIngredients.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedIngredients.toList()) { ingredient ->
                                InputChip(
                                    selected = true,
                                    onClick = { selectedIngredients = selectedIngredients - ingredient },
                                    label = { Text(ingredient) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }

                    Text("Available Ingredients:", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableIngredients) { ingredient ->
                            FilterChip(
                                selected = ingredient in selectedIngredients,
                                onClick = {
                                    selectedIngredients = if (ingredient in selectedIngredients)
                                        selectedIngredients - ingredient
                                    else
                                        selectedIngredients + ingredient
                                },
                                label = { Text(ingredient) }
                            )
                        }
                    }

                    /**
                     * Generate button
                     * - Calls API via ViewModel
                     * - Does NOT navigate away — results appear below on this screen
                     */
                    Button(
                        onClick = {
                            if (selectedIngredients.isNotEmpty()) {
                                recipeViewModel.filterByIngredient(selectedIngredients.first())
                                // NO onNavigateToRecipes() here anymore
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedIngredients.isNotEmpty() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Generate Recipes", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Results header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Generated Recipes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (searchResults.isNotEmpty()) {
                    TextButton(onClick = { recipeViewModel.clearResults() }) {
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }

            // Results section
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Searching for recipes...", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Red.copy(alpha = 0.7f)
                            )
                            Text(errorMessage ?: "Something went wrong", color = Color.Red)
                            TextButton(onClick = { recipeViewModel.clearError() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                searchResults.isEmpty() -> {
                    // Idle state — nothing searched yet
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Search, null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Text("No recipes generated yet", fontSize = 14.sp, color = Color.Gray)
                            Text(
                                "Select ingredients and click Generate",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                else -> {
                    // Show API results as tappable cards
                    // Height is fixed so it doesn't fight with the outer verticalScroll
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(400.dp),
                        userScrollEnabled = false
                    ) {
                        items(searchResults) { meal ->
                            GeneratedRecipeCard(
                                meal = meal,
                                onClick = { onRecipeClick(meal) }  // tapping opens detail
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recipe Card
 */
@Composable
private fun GeneratedRecipeCard(
    meal: MealDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Home, null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(8.dp)
            ) {
                Text(meal.strMeal, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(meal.strCategory ?: "Recipe", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}