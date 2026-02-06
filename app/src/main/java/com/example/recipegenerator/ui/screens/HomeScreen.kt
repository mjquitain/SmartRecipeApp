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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.model.Recipe
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

/**
 * HomeScreen - Recipe Generation Page
 * "Want to look for a meal?" - ingredient input and recipe generation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues = PaddingValues(),
    onProfileClick: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
    var generatedRecipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

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
                    IconButton(onClick = { /* TODO: Share */ }) {
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
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
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

                    // Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter ingredient...") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank() && inputText.lowercase() !in selectedIngredients) {
                                        selectedIngredients = selectedIngredients + inputText.lowercase().trim()
                                        inputText = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, "Add ingredient")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    // Selected Ingredients
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
                                    selectedIngredients = if (ingredient in selectedIngredients) {
                                        selectedIngredients - ingredient
                                    } else {
                                        selectedIngredients + ingredient
                                    }
                                },
                                label = { Text(ingredient) }
                            )
                        }
                    }

                    // Generate Button
                    Button(
                        onClick = {
                            generatedRecipes = listOf(
                                Recipe(id = "g1", name = "${selectedIngredients.firstOrNull() ?: "Chicken"} Salad", description = "Fresh and healthy", cookingTime = 20, difficulty = "Easy"),
                                Recipe(id = "g2", name = "${selectedIngredients.elementAtOrNull(1) ?: "Egg"} Fried Rice", description = "Quick and easy", cookingTime = 25, difficulty = "Easy"),
                                Recipe(id = "g3", name = "${selectedIngredients.firstOrNull() ?: "Spinach"} Soup", description = "Nutritious meal", cookingTime = 30, difficulty = "Medium"),
                                Recipe(id = "g4", name = "Mixed Vegetable Stir Fry", description = "Healthy combo", cookingTime = 15, difficulty = "Easy")
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedIngredients.isNotEmpty()
                    ) {
                        Text("Generate Recipes", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Generated Recipes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Generated Recipes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (generatedRecipes.isNotEmpty()) {
                    TextButton(onClick = { generatedRecipes = emptyList() }) {
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }

            if (generatedRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                        Text("No recipes generated yet", fontSize = 14.sp, color = Color.Gray)
                        Text("Select ingredients and click Generate", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(400.dp)
                ) {
                    items(generatedRecipes) { recipe ->
                        SimpleRecipeCard(recipe = recipe, onClick = onNavigateToRecipes)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
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
                Icon(Icons.Default.Home, null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(8.dp)
            ) {
                Text(recipe.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${recipe.cookingTime} min • ${recipe.difficulty}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RecipeGeneratorTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen()
    }
}