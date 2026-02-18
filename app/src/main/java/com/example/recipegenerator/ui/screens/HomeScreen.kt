package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.recipegenerator.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    padding: PaddingValues = PaddingValues(),
    recipeViewModel: RecipeViewModel,          // Injected from NaviSetHomeDestinations
    onProfileClick: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }

    // Observing ViewModel StateFlows
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()

    // Quick-select ingredient suggestions
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

                    // Manual ingredient input field
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

                    // Chips showing what the user has selected — tap to remove
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

                    // Quick-select chips
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

                    Button(
                        onClick = {
                            if (selectedIngredients.isNotEmpty()) {
                                recipeViewModel.filterByIngredient(selectedIngredients.first())
                                onNavigateToRecipes()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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

            // Status section below the card
            // Shows loading spinner, error message, or default empty hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Generated Recipes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            when {
                isLoading -> {
                    // API call in progress
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
                    // API call failed
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
                            Text(
                                errorMessage ?: "Something went wrong",
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                            TextButton(onClick = { recipeViewModel.clearError() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                else -> {
                    // Default idle state — no search done yet
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
                            Text("Select ingredients and click Generate", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}