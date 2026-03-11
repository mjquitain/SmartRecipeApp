package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.network.MealDto
import com.example.recipegenerator.ui.theme.Brown30
import com.example.recipegenerator.ui.theme.Brown50
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel
import com.example.recipegenerator.ui.viewmodel.toEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeGenerationScreen(
    padding: PaddingValues = PaddingValues(),
    recipeViewModel: RecipeViewModel,
    onProfileClick: () -> Unit = {},
    onRecipeClick: (RecipeEntity) -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val apiResults by recipeViewModel.searchResultsWithFavorites.collectAsState()
    val favoriteRecipes by recipeViewModel.favoriteRecipes.collectAsState()

    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by recipeViewModel.snackbarMessage. collectAsState(initial = null)

    LaunchedEffect(Unit) {
        recipeViewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var maxCookingTime by remember { mutableIntStateOf(120) }
    var sortBy by remember { mutableStateOf("name") }
    var recipeToToggle by remember { mutableStateOf<RecipeEntity?>(null)}
    var showConfirmDialog by remember { mutableStateOf(false) }

    val activeCategory by recipeViewModel.selectedCategory.collectAsState()

    val filteredFavorites = favoriteRecipes.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val categories = listOf("Beef", "Chicken", "Vegetarian", "Vegan", "Dessert", "Lamb", "Miscellaneous", "Pasta", "Seafood", "Side", "Pork", "Breakfast", "Goat", "Starter")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Brown50,
                indicator = { },
                divider = { },
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .height(45.dp)
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedTab == 0) Brown30 else Color.Transparent)
                ) {
                    Text(
                        "Available Recipes",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Color.Black else Color.White
                    )
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selectedTab == 1) Brown30 else Color.Transparent)
                ) {
                    Text(
                        "Favorite Recipes",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) Color.Black else Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = {
//                        searchQuery = it
//                        if (it.length >= 3) {
//                            recipeViewModel.searchRecipes(it)
//                        } else if (it.isEmpty()) {
//                            recipeViewModel.searchRecipes("s")
//                        }
//                    },
//                    modifier = Modifier.weight(0.6f),
//                    placeholder = { Text("Search recipes...") },
//                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
//                    trailingIcon = {
//                        if (searchQuery.isNotEmpty()) {
//                            IconButton(onClick = {
//                                searchQuery = ""
//                                recipeViewModel.searchRecipes("s")
//                            }) {
//                                Icon(Icons.Default.Close, "Clear")
//                            }
//                        }
//                    },
//                    shape = RoundedCornerShape(12.dp),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
//                        focusedContainerColor = Color.LightGray.copy(alpha = 0.2f)
//                    ),
//                    singleLine = true
//                )
//
//                Box {
//                    IconButton(
//                        onClick = { showFilterDialog = true },
//                        modifier = Modifier
//                            .size(48.dp)
//                            .background(
//                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//                                RoundedCornerShape(12.dp)
//                            )
//                    ) {
//                        Icon(Icons.Default.Menu, "Filter", tint = MaterialTheme.colorScheme.primary)
//                    }
//                    if (selectedDifficulty != null || maxCookingTime < 120) {
//                        Box(
//                            Modifier
//                                .size(12.dp)
//                                .clip(CircleShape)
//                                .background(Color.Red)
//                                .align(Alignment.TopEnd)
//                        )
//                    }
//                }
//            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 3) {
                        recipeViewModel.searchRecipes(it)
                    } else if (it.isEmpty()) {
                        recipeViewModel.searchRecipes("s")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                placeholder = { Text("Search recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            recipeViewModel.resetToDefault()
                        }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(34.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(horizontal = 3.dp)
            ) {
                items(categories) { itemCategory ->
                    FilterChip(
                        selected = activeCategory == itemCategory,
                        onClick = {
                            if (activeCategory == itemCategory) {
                                recipeViewModel.resetToDefault()
                            } else {
                                searchQuery = ""
                                recipeViewModel.filterByCategory(itemCategory)
                            }
                        },
                        label = { Text(itemCategory) },
                        shape = RoundedCornerShape(20.dp),
                        leadingIcon = if (activeCategory == itemCategory) {
                            {Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))}
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Brown30,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Active filter chips (only relevant for Tab 1)
//            if (selectedTab == 1 && (selectedDifficulty != null || maxCookingTime < 120 || sortBy != "name")) {
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    if (selectedDifficulty != null) {
//                        FilterChip(
//                            selected = true,
//                            onClick = { selectedDifficulty = null },
//                            label = { Text(selectedDifficulty!!) },
//                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
//                        )
//                    }
//                    if (maxCookingTime < 120) {
//                        FilterChip(
//                            selected = true,
//                            onClick = { maxCookingTime = 120 },
//                            label = { Text("≤ $maxCookingTime min") },
//                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
//                        )
//                    }
//                    if (sortBy != "name") {
//                        FilterChip(
//                            selected = true,
//                            onClick = { sortBy = "name" },
//                            label = { Text("Sort: $sortBy") },
//                            trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp)) }
//                        )
//                    }
//                }
//            }

//            Spacer(Modifier.height(16.dp))

            if (showConfirmDialog && recipeToToggle != null) {
                val isCurrentlyFavorite = favoriteRecipes.any { it.remoteId == recipeToToggle?.remoteId }

                AlertDialog(
                    onDismissRequest = {
                        showConfirmDialog = false
                        recipeToToggle = null
                    },
                    title = { Text(if (isCurrentlyFavorite) "Remove Favorite?" else "Add to Favorites?") },
                    text = { Text("Do you want to ${if (isCurrentlyFavorite) "remove" else "add"} '${recipeToToggle?.name}' ${if (isCurrentlyFavorite) "from" else "to"} your collection?") },
                    confirmButton = {
                        Button(onClick = {
                            recipeViewModel.toggleFavorite(recipeToToggle!!)
                            showConfirmDialog = false
                            recipeToToggle = null
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showConfirmDialog = false
                            recipeToToggle = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            when {
                isLoading && apiResults.isEmpty() && favoriteRecipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    ErrorView(errorMessage = errorMessage!!) { recipeViewModel.clearError() }
                }
                else -> {
                    when (selectedTab) {
                        0 -> ApiRecipesGrid(
                            apiResults = apiResults,
                            onMealClick = { mealId ->
                                recipeViewModel.selectMeal(mealId)
                                onRecipeClick(
                                    RecipeEntity(
                                        remoteId = mealId,
                                        name = "", imageUrl = "", category = "",
                                        area = "", ingredients = "", instruction = "",
                                        isFavorite = false
                                    )
                                )
                            },
                            onFavoritesClick = { mealDto ->
                                recipeToToggle = mealDto.toEntity()
                                showConfirmDialog = true
                            },
                            onNavigateToHome = onNavigateToHome,
                        )
                        1 -> FavoritesGrid(
                            favorites = filteredFavorites,
                            onFavoritesClick = { recipeEntity ->
                                recipeToToggle = recipeEntity
                                showConfirmDialog = true},
                            onRecipeClick = { recipe -> onRecipeClick(recipe) }
                        )
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
private fun ApiRecipeCard(meal: MealDto, onFavoriteClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = meal.strMealThumb,
                contentDescription = meal.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
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
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = if (meal.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (meal.isFavorite) Color.Red else Color.Gray
                )
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
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onCardClick
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
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
//                Text("${recipe.cookingTime} min • ${recipe.difficulty}", fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (recipe.isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ApiRecipesGrid(
    apiResults: List<MealDto>,
    onMealClick: (String) -> Unit,
    onFavoritesClick: (MealDto) -> Unit,
    onNavigateToHome: () -> Unit
) {
    if (apiResults.isEmpty()) {
        EmptyStateView(
            message = "No recipes found. Try generating from Home.",
            actionText = "Go to Home",
            onAction = onNavigateToHome
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(apiResults) { meal ->
                ApiRecipeCard(meal = meal, onFavoriteClick = { onFavoritesClick(meal) }, onClick = { onMealClick(meal.idMeal) })
            }
        }
    }
}

@Composable
fun FavoritesGrid(
    favorites: List<RecipeEntity>,
    onFavoritesClick: (RecipeEntity) -> Unit,
    onRecipeClick: (RecipeEntity) -> Unit
) {
    if (favorites.isEmpty()) {
        EmptyStateView(message = "No favorite recipes yet")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items (favorites, key = { it.id }) {recipe ->
                LocalRecipeCard(recipe = recipe, onCardClick = { onRecipeClick(recipe) }, onFavoriteClick = { onFavoritesClick(recipe) })
            }
        }
    }
}

@Composable
fun ErrorView(errorMessage: String, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, null, modifier = Modifier.size(48.dp), tint = Color.Red)
            Text(errorMessage, color = Color.Red)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
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
                message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (actionText != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(recipe: RecipeEntity, onBackClick: () -> Unit, onFavoriteClick: () -> Unit,) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp, 30.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp, 30.dp)
                    .background(Color.White.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (recipe.isFavorite) Color.Red else Color.White
                )
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                SuggestionChip(onClick = {}, label = { Text(recipe.category) })
                Spacer(Modifier.width(8.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = recipe.ingredients,
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(16.dp))

            Text("Instructions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = recipe.instruction,
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
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