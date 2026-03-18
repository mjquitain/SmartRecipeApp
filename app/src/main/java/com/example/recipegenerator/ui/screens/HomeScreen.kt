package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.recipegenerator.network.MealDto
import com.example.recipegenerator.ui.theme.Brown30
import com.example.recipegenerator.ui.theme.Brown50
import com.example.recipegenerator.ui.theme.SurfaceWhite
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel
import com.example.recipegenerator.ui.theme.scaledSp

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

    LaunchedEffect(Unit) { recipeViewModel.clearGeneratedResults() }

    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()
    val searchResults by recipeViewModel.generatedWithFavorites.collectAsState()
    val myPantry by ingredientViewModel.ingredients.collectAsState()
    val availableIngredients = myPantry.map { it.name }.distinct()

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        "Home",
                        fontSize = scaledSp(20f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Share,
                            "Share",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, "Profile", tint = MaterialTheme.colorScheme.onPrimary)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ─── Recipe Generation Card ───────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
                        Text(
                            "Want to look for a meal?",
                            fontSize = scaledSp(16f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Enter the ingredients and we'll suggest delicious recipes.",
                            fontSize = scaledSp(14f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Enter ingredient...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val trimmed = inputText.trim().lowercase()
                                    if (trimmed.isNotBlank() && trimmed !in selectedIngredients) {
                                        selectedIngredients = selectedIngredients + trimmed
                                    }
                                    inputText = ""
                                }) {
                                    Icon(
                                        Icons.Default.Add,
                                        "Add ingredient",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val trimmed = inputText.trim().lowercase()
                                    if (trimmed.isNotBlank() && trimmed !in selectedIngredients) {
                                        selectedIngredients = selectedIngredients + trimmed
                                    }
                                    inputText = ""
                                }
                            ),
                            shape = RoundedCornerShape(30.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))

                        // Selected ingredients
                        if (selectedIngredients.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(selectedIngredients.toList()) { ingredient ->
                                    InputChip(
                                        selected = true,
                                        onClick = {
                                            selectedIngredients = selectedIngredients - ingredient
                                        },
                                        label = { Text(ingredient, fontSize = scaledSp(13f)) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                "Remove",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Available Ingredients from Room
                        if (availableIngredients.isNotEmpty()) {
                            Text(
                                "Available Ingredients:",
                                fontSize = scaledSp(14f),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(availableIngredients) { ingredient ->
                                    FilterChip(
                                        selected = ingredient in selectedIngredients,
                                        onClick = {
                                            selectedIngredients =
                                                if (ingredient in selectedIngredients)
                                                    selectedIngredients - ingredient
                                                else selectedIngredients + ingredient
                                        },
                                        label = { Text(ingredient, fontSize = scaledSp(13f)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = ingredient in selectedIngredients,
                                            borderColor = MaterialTheme.colorScheme.outline,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                                            borderWidth = 1.dp,
                                            selectedBorderWidth = 1.dp
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Generate button
                        Button(
                            onClick = {
                                if (selectedIngredients.isNotEmpty()) {
                                    hasSearched = true
                                    recipeViewModel.filterByIngredient(selectedIngredients.first())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = selectedIngredients.isNotEmpty() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Generate Recipes", fontSize = scaledSp(16f), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ─── Generated Recipes Header ─────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Generated Recipes",
                        fontSize = scaledSp(16f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (searchResults.isNotEmpty()) {
                        TextButton(onClick = { recipeViewModel.clearGeneratedResults() }) {
                            Text(
                                "Clear",
                                fontSize = scaledSp(12f),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ─── Results ─────────────────────────────────────────────────
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    "Searching for recipes...",
                                    fontSize = scaledSp(14f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                errorMessage != null -> {
                    item {
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
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    errorMessage ?: "Something went wrong",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = scaledSp(14f)
                                )
                                TextButton(onClick = { recipeViewModel.clearError() }) {
                                    Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                !hasSearched || searchResults.isEmpty() -> {
                    item {
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    "No recipes generated yet",
                                    fontSize = scaledSp(14f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Select ingredients and click Generate",
                                    fontSize = scaledSp(12f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
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
                            if (rowMeals.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedRecipeCard(meal: MealDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.strMealThumb).crossfade(true).build(),
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
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Text(
                    meal.strMeal,
                    fontSize = scaledSp(14f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    meal.strCategory ?: "Recipe",
                    fontSize = scaledSp(11f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}