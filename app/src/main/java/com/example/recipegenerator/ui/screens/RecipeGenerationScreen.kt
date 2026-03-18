package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.recipegenerator.data.entity.RecipeEntity
import com.example.recipegenerator.network.MealDto
import com.example.recipegenerator.ui.theme.Brown50
import com.example.recipegenerator.ui.theme.SurfaceWhite
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel
import com.example.recipegenerator.ui.viewmodel.toEntity
import com.example.recipegenerator.ui.theme.scaledSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeGenerationScreen(
    padding: PaddingValues = PaddingValues(),
    recipeViewModel: RecipeViewModel,
    onProfileClick: () -> Unit = {},
    onRecipeClick: (RecipeEntity) -> Unit = {},
) {
    val apiResults by recipeViewModel.searchResults.collectAsState()
    val favoriteRecipes by recipeViewModel.favoriteRecipes.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()
    val favoriteIds by recipeViewModel.favoriteIds.collectAsState()
    val activeCategory by recipeViewModel.selectedCategory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        recipeViewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val selectedTab by recipeViewModel.selectedTab.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var recipeToToggle by remember { mutableStateOf<RecipeEntity?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val filteredFavorites = favoriteRecipes.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val categories = listOf(
        "Beef", "Chicken", "Vegetarian", "Vegan", "Dessert",
        "Lamb", "Miscellaneous", "Pasta", "Seafood", "Side",
        "Pork", "Breakfast", "Goat", "Starter"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recipe Dashboard",
                        fontSize = scaledSp(20f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Share, "Share",
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
                        Icon(
                            Icons.Default.Person, "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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

            // ─── Tab Row ──────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                indicator = { },
                divider = { },
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .height(45.dp)
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { recipeViewModel.setTab(0) },  // CHANGED
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selectedTab == 0) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                ) {
                    Text(
                        "Available Recipes",
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp),
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { recipeViewModel.setTab(1) },  // CHANGED
                    modifier = Modifier
                        .padding(5.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selectedTab == 1) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                ) {
                    Text(
                        "Favorite Recipes",
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp),
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Search Field ─────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 3) recipeViewModel.searchRecipes(it)
                    else if (it.isEmpty()) recipeViewModel.resetToDefault()
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                placeholder = {
                    Text(
                        "Search recipes...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search, "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            recipeViewModel.resetToDefault()
                        }) {
                            Icon(
                                Icons.Default.Close, "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(34.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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

            // ─── Category Chips ───────────────────────────────────────────
            if (selectedTab == 0) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                                {
                                    Icon(
                                        Icons.Default.Check, null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activeCategory == itemCategory,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ─── Confirm Dialog ───────────────────────────────────────────
            if (showConfirmDialog && recipeToToggle != null) {
                val isCurrentlyFavorite = favoriteIds.contains(recipeToToggle?.remoteId)
                AlertDialog(
                    onDismissRequest = {
                        showConfirmDialog = false
                        recipeToToggle = null
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            if (isCurrentlyFavorite) "Remove Favorite?" else "Add to Favorites?",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            "Do you want to ${if (isCurrentlyFavorite) "remove" else "add"} " +
                                    "'${recipeToToggle?.name}' " +
                                    "${if (isCurrentlyFavorite) "from" else "to"} your collection?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                recipeViewModel.toggleFavorite(
                                    recipeId = recipeToToggle?.remoteId ?: "",
                                    isNowFavorite = !isCurrentlyFavorite,
                                    recipeEntity = recipeToToggle
                                )
                                showConfirmDialog = false
                                recipeToToggle = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text("Confirm") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showConfirmDialog = false
                                recipeToToggle = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("Cancel") }
                    }
                )
            }

            // ─── Content ──────────────────────────────────────────────────
            when {
                isLoading && apiResults.isEmpty() && favoriteRecipes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                            TextButton(
                                onClick = { recipeViewModel.clearError() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("Dismiss") }
                        }
                    }
                }
                else -> {
                    when (selectedTab) {
                        0 -> ApiRecipesGrid(
                            apiResults = apiResults,
                            favoriteIds = favoriteIds,
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
                            onFavoritesClick = { mealDto, _ ->
                                recipeToToggle = mealDto.toEntity()
                                showConfirmDialog = true
                            }
                        )
                        1 -> FavoritesGrid(
                            favorites = filteredFavorites,
                            onFavoritesClick = { recipeEntity ->
                                recipeToToggle = recipeEntity
                                showConfirmDialog = true
                            },
                            onRecipeClick = { recipe -> onRecipeClick(recipe) }
                        )
                    }
                }
            }
        }
    }
}

// ─── API Recipe Card ──────────────────────────────────────────────────────────
@Composable
private fun ApiRecipeCard(
    meal: MealDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
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
                    meal.strMeal, fontSize = scaledSp(14f), fontWeight = FontWeight.Bold,
                    maxLines = 1, color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    meal.strCategory ?: "Recipe",
                    fontSize = scaledSp(11f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Local Favorites Card ─────────────────────────────────────────────────────
@Composable
private fun LocalRecipeCard(
    recipe: RecipeEntity,
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onCardClick
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl).crossfade(true).build(),
                contentDescription = recipe.name,
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
                    recipe.name, fontSize = scaledSp(14f), fontWeight = FontWeight.Bold,
                    maxLines = 1, color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    recipe.category ?: "Recipe",
                    fontSize = scaledSp(11f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (recipe.isFavorite) Color.Red
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Grids ────────────────────────────────────────────────────────────────────
@Composable
fun ApiRecipesGrid(
    apiResults: List<MealDto>,
    favoriteIds: List<String>,
    onMealClick: (String) -> Unit,
    onFavoritesClick: (MealDto, Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(apiResults, key = { it.idMeal }) { meal ->
            val isFavorite = favoriteIds.contains(meal.idMeal)
            ApiRecipeCard(
                meal = meal,
                isFavorite = isFavorite,
                onFavoriteClick = { onFavoritesClick(meal, isFavorite) },
                onClick = { onMealClick(meal.idMeal) }
            )
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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    "No favorite recipes yet",
                    fontSize = scaledSp(14f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            items(favorites, key = { it.remoteId }) { recipe ->
                LocalRecipeCard(
                    recipe = recipe,
                    onCardClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoritesClick(recipe) }
                )
            }
        }
    }
}

// ─── Recipe Detail Screen ─────────────────────────────────────────────────────
@Composable
fun RecipeDetailScreen(
    padding: PaddingValues = PaddingValues(),
    recipe: RecipeEntity,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp, 30.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack, "Back",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp, 30.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) Color.Red else Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                recipe.category,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    "Ingredients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = recipe.ingredients,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Instructions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = recipe.instruction,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}