package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.network.MealDto
import com.example.recipegenerator.ui.theme.Brown30
import com.example.recipegenerator.ui.theme.Brown50
import com.example.recipegenerator.ui.theme.Lime10
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel
import com.example.recipegenerator.ui.viewmodel.toEntity

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
    ingredientViewModel: com.example.recipegenerator.ui.viewmodel.IngredientViewModel,
    onProfileClick: () -> Unit = {},
    onRecipeClick: (MealDto) -> Unit = {}

) {
    var inputText by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        recipeViewModel.clearGeneratedResults()
    }

    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()
    val searchResults by recipeViewModel.generatedWithFavorites.collectAsState()
    val myPantry by ingredientViewModel.ingredients.collectAsState()
    val availableIngredients = myPantry.map { it.name }.distinct()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Lime10),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Brown50
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .background(Brown30),
                    ) {
                        Text(
                            "Want to look for a meal?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp, start = 14.dp, end = 14.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Enter the ingredients and we'll suggest delicious recipes.",
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
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
                            shape = RoundedCornerShape(30.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        if (selectedIngredients.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 14.dp)) {
                                items(selectedIngredients.toList()) { ingredient ->
                                    InputChip(
                                        selected = true,
                                        onClick = { selectedIngredients = selectedIngredients - ingredient },
                                        label = { Text(ingredient) },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
                                        },
                                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = Color.White, selectedLabelColor = Color.Black),
                                        shape = RoundedCornerShape(20.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("Available Ingredients:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 14.dp)) {
                            items(availableIngredients) { ingredient ->
                                FilterChip(
                                    selected = ingredient in selectedIngredients,
                                    onClick = {
                                        selectedIngredients = if (ingredient in selectedIngredients)
                                            selectedIngredients - ingredient
                                        else
                                            selectedIngredients + ingredient
                                    },
                                    label = { Text(ingredient) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Brown50,
                                        labelColor = Color.White,
                                        selectedContainerColor = Color.White,
                                        selectedLabelColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (selectedIngredients.isNotEmpty()) {
                                    hasSearched = true
                                    recipeViewModel.filterByIngredient(selectedIngredients.first())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp, start = 14.dp, end = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContentColor = Color.Black,
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
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
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Generated Recipes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (searchResults.isNotEmpty()) {
                        TextButton(onClick = { recipeViewModel.clearGeneratedResults() }) {
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }
            }

            when {
                isLoading -> {
                    item {
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
                                CircularProgressIndicator()
                                Text("Searching for recipes...", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                errorMessage != null -> {
                    item {
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
                }

                !hasSearched || searchResults.isEmpty() -> {
                    item {
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
                                Icon(
                                    Icons.Default.Search, null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                                Text("No recipes generated yet", fontSize = 14.sp, color = Color.Gray)
                                Text("Select ingredients and click Generate", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                else -> {
                    val rows = searchResults.chunked(2)
                    items(rows) { rowMeals ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (meal in rowMeals) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GeneratedRecipeCard(
                                        meal = meal,
                                        onClick = { onRecipeClick(meal) }
                                    )
                                }
                            }
                            if (rowMeals.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
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
            .aspectRatio(0.85f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.strMealThumb)
                    .crossfade(true)
                    .build(),
                contentDescription = meal.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
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