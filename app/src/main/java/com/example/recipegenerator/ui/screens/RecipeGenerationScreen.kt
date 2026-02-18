package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.network.MealDto
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeGenerationScreen(
    padding: PaddingValues = PaddingValues(),
    recipeViewModel: RecipeViewModel,           // Injected from NaviSetHomeDestinations
    onProfileClick: () -> Unit = {},
    onRecipeClick: (RecipeEntity) -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    // Tab 0: API results from MealDB
    val searchResults by recipeViewModel.searchResults.collectAsState()

    // Tab 1: Saved favorites from Room database
    val favoriteRecipes by recipeViewModel.favoriteRecipes.collectAsState()

    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var maxCookingTime by remember { mutableIntStateOf(120) }
    var sortBy by remember { mutableStateOf("name") }

    /**
     * Filtering logic for Tab 0 (API results - MealDto)
     * MealDto only has meal name and image from the filter endpoint,
     * so we can only search by name here.
     */
    val filteredApiResults = searchResults.filter {
        it.strMeal.contains(searchQuery, ignoreCase = true)
    }

    /**
     * Filtering logic for Tab 1 (Room favorites - RecipeEntity)
     * Room entities have full data so we can filter by name, difficulty, and cooking time
     */
    val filteredFavorites = favoriteRecipes
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { if (selectedDifficulty != null) it.difficulty == selectedDifficulty else true }
        .filter { it.cookingTime <= maxCookingTime }
        .let { list ->
            when (sortBy) {
                "time" -> list.sortedBy { it.cookingTime }
                "difficulty" -> list.sortedBy { it.difficulty }
                else -> list.sortedBy { it.name }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Recipe Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.LightGray.copy(alpha = 0.3f),
                indicator = { },
                modifier = Modifier.clip(RoundedCornerShape(24.dp)).height(48.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedTab == 0) Color.White else Color.Transparent)
                ) {
                    Text(
                        "Available Recipes",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Color.Black else Color.Gray
                    )
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedTab == 1) Color.White else Color.Transparent)
                ) {
                    Text(
                        "Favorite Recipes",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search bar + filter button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                        focusedContainerColor = Color.LightGray.copy(alpha = 0.2f)
                    ),
                    singleLine = true
                )

                // Filter button — only useful on Tab 1 (Room data has full fields)
                Box {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(Icons.Default.Menu, "Filter", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (selectedDifficulty != null || maxCookingTime < 120) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Active filter chips (only relevant for Tab 1)
            if (selectedTab == 1 && (selectedDifficulty != null || maxCookingTime < 120 || sortBy != "name")) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedDifficulty != null) {
                        FilterChip(
                            selected = true,
                            onClick = { selectedDifficulty = null },
                            label = { Text(selectedDifficulty!!) },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (maxCookingTime < 120) {
                        FilterChip(
                            selected = true,
                            onClick = { maxCookingTime = 120 },
                            label = { Text("≤ $maxCookingTime min") },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (sortBy != "name") {
                        FilterChip(
                            selected = true,
                            onClick = { sortBy = "name" },
                            label = { Text("Sort: $sortBy") },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Content area
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, null, modifier = Modifier.size(48.dp), tint = Color.Red)
                            Text(errorMessage ?: "Error", color = Color.Red)
                            TextButton(onClick = { recipeViewModel.clearError() }) { Text("Dismiss") }
                        }
                    }
                }

                selectedTab == 0 -> {
                    // TAB 0: API results (MealDto)
                    val count = filteredApiResults.size
                    Text(
                        "$count recipe${if (count != 1) "s" else ""} found",
                        fontSize = 12.sp, color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    if (filteredApiResults.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search, null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                Text(
                                    "No recipes found. Try generating from Home.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                TextButton(onClick = onNavigateToHome) {
                                    Text("Go to Home")
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredApiResults) { meal ->
                                // API result card — uses MealDto fields
                                ApiRecipeCard(meal = meal)
                            }
                        }
                    }
                }

                selectedTab == 1 -> {
                    // TAB 1: Room favorites (RecipeEntity)
                    val count = filteredFavorites.size
                    Text(
                        "$count favorite${if (count != 1) "s" else ""}",
                        fontSize = 12.sp, color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    if (filteredFavorites.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Favorite, null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                Text(
                                    "No favorite recipes yet",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredFavorites, key = { it.id }) { recipe ->
                                // Room entity card — has full data including favorite toggle
                                LocalRecipeCard(
                                    recipe = recipe,
                                    onFavoriteClick = {
                                        recipeViewModel.toggleFavorite(recipe)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        RecipeFilterDialog(
            selectedDifficulty = selectedDifficulty,
            maxCookingTime = maxCookingTime,
            sortBy = sortBy,
            onDifficultyChange = { selectedDifficulty = it },
            onCookingTimeChange = { maxCookingTime = it },
            onSortChange = { sortBy = it },
            onDismiss = { showFilterDialog = false },
            onClearAll = {
                selectedDifficulty = null
                maxCookingTime = 120
                sortBy = "name"
            }
        )
    }
}

/**
 * Card for Tab 0 — API results (MealDto)
 * MealDB filter endpoint only returns meal name and thumbnail,
 * so cooking time and difficulty are not available here.
 */
@Composable
private fun ApiRecipeCard(meal: MealDto) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Placeholder image area — wire Coil here later for real thumbnails
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

/**
 * Card for Tab 1 — Room saved favorites (RecipeEntity)
 */
@Composable
private fun LocalRecipeCard(
    recipe: RecipeEntity,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
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
                Text(recipe.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${recipe.cookingTime} min • ${recipe.difficulty}", fontSize = 11.sp, color = Color.Gray)
            }
            // Favorite toggle button — persists to Room
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .clickable { onFavoriteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Favorite",
                    tint = if (recipe.isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Filter dialog — only used in Tab 1 (Room data)
 */
@Composable
private fun RecipeFilterDialog(
    selectedDifficulty: String?,
    maxCookingTime: Int,
    sortBy: String,
    onDifficultyChange: (String?) -> Unit,
    onCookingTimeChange: (Int) -> Unit,
    onSortChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close") }
                }

                Text("Difficulty", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { onDifficultyChange(if (selectedDifficulty == difficulty) null else difficulty) },
                            label = { Text(difficulty) }
                        )
                    }
                }

                Text("Max Cooking Time: $maxCookingTime min", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Slider(
                    value = maxCookingTime.toFloat(),
                    onValueChange = { onCookingTimeChange(it.toInt()) },
                    valueRange = 15f..120f,
                    steps = 20
                )

                Text("Sort By", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("name" to "Name", "time" to "Time", "difficulty" to "Difficulty").forEach { (value, label) ->
                        FilterChip(
                            selected = sortBy == value,
                            onClick = { onSortChange(value) },
                            label = { Text(label) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onClearAll, modifier = Modifier.weight(1f)) { Text("Clear All") }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Apply") }
                }
            }
        }
    }
}