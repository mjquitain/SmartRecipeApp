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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.recipegenerator.model.Recipe
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

/**
 * RecipeGenerationScreen - Available and Favorite Recipes
 * Shows recipe list with tabs, search, and filter functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeGenerationScreen(
    padding: PaddingValues = PaddingValues(),
    onProfileClick: () -> Unit = {},
    onRecipeClick: (Recipe) -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filter states
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var maxCookingTime by remember { mutableIntStateOf(120) }
    var sortBy by remember { mutableStateOf("name") }

    // Sample recipe data
    var availableRecipes by remember {
        mutableStateOf(
            listOf(
                Recipe(id = "1", name = "Chicken Adobo", description = "Classic Filipino dish", cookingTime = 45, difficulty = "Easy", isFavorite = false),
                Recipe(id = "2", name = "Pasta Carbonara", description = "Creamy Italian pasta", cookingTime = 30, difficulty = "Medium", isFavorite = true),
                Recipe(id = "3", name = "Beef Stew", description = "Hearty comfort food", cookingTime = 120, difficulty = "Hard", isFavorite = false),
                Recipe(id = "4", name = "Caesar Salad", description = "Fresh and crispy", cookingTime = 15, difficulty = "Easy", isFavorite = true),
                Recipe(id = "5", name = "Grilled Salmon", description = "Healthy seafood", cookingTime = 25, difficulty = "Easy", isFavorite = false),
                Recipe(id = "6", name = "Vegetable Stir Fry", description = "Quick and nutritious", cookingTime = 20, difficulty = "Easy", isFavorite = true),
                Recipe(id = "7", name = "Spaghetti Bolognese", description = "Classic meat sauce", cookingTime = 50, difficulty = "Medium", isFavorite = false),
                Recipe(id = "8", name = "Chicken Curry", description = "Spicy and flavorful", cookingTime = 40, difficulty = "Medium", isFavorite = true)
            )
        )
    }

    // Toggle favorite
    fun toggleFavorite(recipeId: String) {
        availableRecipes = availableRecipes.map {
            if (it.id == recipeId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    // Apply filters
    val recipesByTab = if (selectedTab == 0) availableRecipes else availableRecipes.filter { it.isFavorite }
    val searchedRecipes = recipesByTab.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
    }
    val difficultyFiltered = if (selectedDifficulty != null) searchedRecipes.filter { it.difficulty == selectedDifficulty } else searchedRecipes
    val timeFiltered = difficultyFiltered.filter { it.cookingTime <= maxCookingTime }
    val displayedRecipes = when (sortBy) {
        "name" -> timeFiltered.sortedBy { it.name }
        "time" -> timeFiltered.sortedBy { it.cookingTime }
        "difficulty" -> timeFiltered.sortedBy { it.difficulty }
        else -> timeFiltered
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
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

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.LightGray.copy(alpha = 0.3f),
                indicator = { },
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .height(48.dp)
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

            // Search and Filter
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

            // Active filters
            if (selectedDifficulty != null || maxCookingTime < 120 || sortBy != "name") {
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

            Text(
                text = "${displayedRecipes.size} recipe${if (displayedRecipes.size != 1) "s" else ""} found",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Recipe Grid
            if (displayedRecipes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (selectedTab == 1) Icons.Default.Favorite else Icons.Default.Search,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            if (selectedTab == 1) "No favorite recipes yet" else "No recipes found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (searchQuery.isNotEmpty() || selectedDifficulty != null || maxCookingTime < 120) {
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedDifficulty = null
                                maxCookingTime = 120
                            }) {
                                Text("Clear all filters")
                            }
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
                    items(displayedRecipes) { recipe ->
                        RecipeListCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe) },
                            onFavoriteClick = { toggleFavorite(recipe.id) }
                        )
                    }
                }
            }
        }
    }

    // Filter Dialog
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

@Composable
private fun RecipeListCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
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
                    OutlinedButton(onClick = onClearAll, modifier = Modifier.weight(1f)) {
                        Text("Clear All")
                    }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeGenerationScreenPreview() {
    RecipeGeneratorTheme(darkTheme = false, dynamicColor = false) {
        RecipeGenerationScreen()
    }
}