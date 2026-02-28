package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.sharp.Add
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
import com.example.recipegenerator.data.entity.IngredientEntity
import com.example.recipegenerator.ui.components.MinimalListItem
import com.example.recipegenerator.ui.viewmodel.IngredientViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsListScreen(
    padding: PaddingValues = PaddingValues(),
    ingredientViewModel: IngredientViewModel,
    onProfileClick: () -> Unit = {}
) {
    // Observe Room database via StateFlow — updates automatically on any CRUD
    val ingredients by ingredientViewModel.ingredients.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var ingredientToEdit by remember { mutableStateOf<IngredientEntity?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredIngredients = if (selectedCategory != null) {
        ingredients.filter { it.category == selectedCategory }
    } else {
        ingredients
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Ingredients", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Share, "Share")
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, "Profile", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                }
            )
        }
    ) {
        val listScroller = rememberScrollableState { 0.0f }

        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier.padding(16.dp, 20.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val autoWeight = Modifier.weight(1f)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${filteredIngredients.size} ingredient${if (filteredIngredients.size != 1) "s" else ""}",
                        fontSize = 20.sp,
                        modifier = autoWeight
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shrunkenButtonModifier = Modifier.padding(0.dp).size(30.dp)
                        IconButton(
                            modifier = shrunkenButtonModifier,
                            onClick = { showAddDialog = true }
                        ) {
                            Icon(imageVector = Icons.Sharp.Add, contentDescription = "Add ingredient")
                        }
                        IconButton(
                            modifier = shrunkenButtonModifier,
                            onClick = { showFilterDialog = true }
                        ) {
                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "Filter")
                        }
                    }
                }

                if (selectedCategory != null) {
                    FilterChip(
                        selected = true,
                        onClick = { selectedCategory = null },
                        label = { Text(selectedCategory!!) },
                        trailingIcon = {
                            Icon(Icons.Outlined.Close, "Remove filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }

                if (filteredIngredients.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ShoppingCart, null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Text("No ingredients found", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .scrollable(state = listScroller, orientation = Orientation.Vertical),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredIngredients, key = { it.id }) { ingredient ->
                            IngredientListItem(
                                ingredient = ingredient,
                                onEditClick = {
                                    ingredientToEdit = ingredient
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    // DELETE — calls Room via ViewModel
                                    ingredientViewModel.delete(ingredient)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        IngredientDialog(
            title = "Add Ingredient",
            onDismiss = { showAddDialog = false },
            onSave = { newIngredient ->
                // INSERT — calls Room via ViewModel
                ingredientViewModel.insert(newIngredient)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && ingredientToEdit != null) {
        IngredientDialog(
            title = "Edit Ingredient",
            ingredient = ingredientToEdit,
            onDismiss = {
                showEditDialog = false
                ingredientToEdit = null
            },
            onSave = { updatedIngredient ->
                // UPDATE — calls Room via ViewModel
                ingredientViewModel.update(updatedIngredient)
                showEditDialog = false
                ingredientToEdit = null
            }
        )
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            selectedCategory = selectedCategory,
            availableCategories = ingredients.map { it.category }.distinct(),
            onCategorySelect = { category ->
                selectedCategory = category
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun IngredientListItem(
    ingredient: IngredientEntity,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val containerTitleFontSize = 16.sp
    val selectableContainerAlignment = Alignment.CenterVertically
    val titleContainer = Modifier.fillMaxSize().padding(16.dp, 12.dp)
    val actionsContainer = Modifier
        .defaultMinSize(100.dp)
        .width(100.dp)
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.primary)

    val daysUntilExpiration = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expDate = sdf.parse(ingredient.expirationDate)
        val today = Date()
        val diff = expDate!!.time - today.time
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) { 999 }

    val expirationColor = when {
        daysUntilExpiration < 0 -> Color(0.9f, 0.1f, 0.1f)
        daysUntilExpiration <= 3 -> Color(0.9f, 0.5f, 0.1f)
        daysUntilExpiration <= 7 -> Color(0.9f, 0.9f, 0.1f)
        else -> Color(0.3f, 0.9f, 0.1f)
    }

    MinimalListItem {
        Row(modifier = Modifier.fillMaxSize()) {
            val autoWeight = Modifier.weight(1f)
            Row(
                modifier = titleContainer.weight(1.0f),
                verticalAlignment = selectableContainerAlignment,
            ) {
                Column(modifier = autoWeight) {
                    Text(ingredient.name, fontSize = containerTitleFontSize, fontWeight = FontWeight.Medium)
                    Text(
                        "${ingredient.quantity} ${ingredient.unit} • ${ingredient.category}",
                        fontSize = 12.sp, color = Color.Gray
                    )
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier.size(20.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(expirationColor)
                )
            }
            Box(modifier = actionsContainer) {
                Row(verticalAlignment = selectableContainerAlignment) {
                    IconButton(onClick = onEditClick, modifier = autoWeight) {
                        Icon(Icons.Outlined.Create, "Edit", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onDeleteClick, modifier = autoWeight) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDialog(
    title: String,
    ingredient: IngredientEntity? = null,
    onDismiss: () -> Unit,
    onSave: (IngredientEntity) -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var category by remember { mutableStateOf(ingredient?.category ?: "Vegetable") }
    var quantity by remember { mutableStateOf(ingredient?.quantity ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "g") }
    var expirationDate by remember { mutableStateOf(ingredient?.expirationDate ?: "") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }

    val categories = listOf("Meat", "Seafood", "Vegetable", "Fruit", "Dairy", "Grain", "Spice", "Other")
    val units = listOf("g", "kg", "ml", "l", "pieces", "tbsp", "tsp", "cups")

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
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Close") }
                }

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Ingredient Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; showCategoryMenu = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it },
                        label = { Text("Quantity") }, modifier = Modifier.weight(1f), singleLine = true
                    )
                    ExposedDropdownMenuBox(
                        expanded = showUnitMenu, onExpandedChange = { showUnitMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit, onValueChange = {}, readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = showUnitMenu, onDismissRequest = { showUnitMenu = false }) {
                            units.forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; showUnitMenu = false })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = expirationDate, onValueChange = { expirationDate = it },
                    label = { Text("Expiration Date (yyyy-MM-dd)") },
                    placeholder = { Text("2026-02-20") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && quantity.isNotBlank()) {
                                onSave(
                                    IngredientEntity(
                                        id = ingredient?.id ?: 0,
                                        name = name,
                                        category = category,
                                        quantity = quantity,
                                        unit = unit,
                                        expirationDate = expirationDate
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && quantity.isNotBlank()
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    selectedCategory: String?,
    availableCategories: List<String>,
    onCategorySelect: (String?) -> Unit,
    onDismiss: () -> Unit
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
                    Text("Filter by Category", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Close") }
                }
                Text("Select a category to filter:", fontSize = 14.sp, color = Color.Gray)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onCategorySelect(cat) },
                            label = { Text(cat) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (selectedCategory != null) {
                    OutlinedButton(
                        onClick = { onCategorySelect(null); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Clear Filter") }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Ingredit() {
    IngredientDialog("Sample", null, {}, {})
}