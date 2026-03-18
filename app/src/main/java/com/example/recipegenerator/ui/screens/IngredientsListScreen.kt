package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.recipegenerator.data.entity.IngredientEntity
import com.example.recipegenerator.ui.theme.scaledSp
import com.example.recipegenerator.ui.viewmodel.IngredientViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.math.max

private val expirationDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
private val regexNumeric = Regex("^\\s*(?<number>-?\\d+(?:\\.\\d+)?)\\s*$")

private val expiryFresh = Color(0xFF4CAF50)
private val expiryWeek = Color(0xFFFFEB3B)
private val expiryThreeDays = Color(0xFFFF9800)
private val expiryExpired = Color(0xFFF44336)
private val expiryUnspecified = Color(0xFF90CAF9)
private val expiryError = Color(0xFFCE93D8)

private fun _tryGetDate(from: String): LocalDate? {
    return try { LocalDate.parse(from, expirationDateFormat) }
    catch (e: DateTimeParseException) { null }
}

private fun _getRelativeLifespanFromString(dateString: String): Long {
    if (dateString.isBlank()) return -2L
    val date = _tryGetDate(dateString) ?: return -3L
    return max(date.minusDays(LocalDate.now().toEpochDay()).toEpochDay(), -1)
}

private fun expiryLabel(lifespan: Long): String = when {
    lifespan > 7 -> "Fresh"
    lifespan > 3 -> "Expiring soon"
    lifespan >= 0 -> "Expiring in ${lifespan}d"
    lifespan == -1L -> "Expired"
    lifespan == -2L -> "No expiry set"
    else -> "Invalid date"
}

private fun expiryColor(lifespan: Long): Color = when {
    lifespan > 7 -> expiryFresh
    lifespan > 3 -> expiryWeek
    lifespan >= 0 -> expiryThreeDays
    lifespan == -1L -> expiryExpired
    lifespan == -2L -> expiryUnspecified
    else -> expiryError
}

// ─── Ingredients List Screen ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsListScreen(
    padding: PaddingValues = PaddingValues(),
    ingredientViewModel: IngredientViewModel,
    onProfileClick: () -> Unit = {}
) {
    val ingredients by ingredientViewModel.ingredients.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var ingredientToEdit by remember { mutableStateOf<IngredientEntity?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredIngredients = if (selectedCategory != null)
        ingredients.filter { it.category == selectedCategory }
    else ingredients

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
                        "Ingredients",
                        fontSize = scaledSp(20f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Outlined.Share, "Share",
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
                            Icons.Filled.Person, "Profile",
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Count + buttons row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${filteredIngredients.size} ingredient${if (filteredIngredients.size != 1) "s" else ""}",
                    fontSize = scaledSp(18f),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                // Add button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Sharp.Add, "Add ingredient",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Filter button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showFilterDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.List, "Filter",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Active filter chip
            if (selectedCategory != null) {
                FilterChip(
                    selected = true,
                    onClick = { selectedCategory = null },
                    label = { Text(selectedCategory!!) },
                    trailingIcon = {
                        Icon(Icons.Outlined.Close, "Remove", modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Empty state
            if (filteredIngredients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.ShoppingCart, null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "No ingredients yet",
                            fontSize = scaledSp(16f),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Tap + to add your first ingredient",
                            fontSize = scaledSp(13f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        filteredIngredients,
                        key = { "${it.name}_${it.expirationDate}" }
                    ) { ingredient ->
                        IngredientListItem(
                            ingredient = ingredient,
                            onEditClick = {
                                ingredientToEdit = ingredient
                                showEditDialog = true
                            },
                            onDeleteClick = { ingredientViewModel.delete(ingredient) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        IngredientDialog(
            title = "Add Ingredient",
            userId = ingredientViewModel.userId,
            onDismiss = { showAddDialog = false },
            onSave = { newIngredient ->
                ingredientViewModel.insert(newIngredient)
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && ingredientToEdit != null) {
        IngredientDialog(
            title = "Edit Ingredient",
            userId = ingredientViewModel.userId,
            ingredient = ingredientToEdit,
            onDismiss = { showEditDialog = false; ingredientToEdit = null },
            onSave = { updatedIngredient ->
                ingredientViewModel.update(updatedIngredient)
                showEditDialog = false
                ingredientToEdit = null
            }
        )
    }

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

// ─── Ingredient Card ──────────────────────────────────────────────────────────
@Composable
fun IngredientListItem(
    ingredient: IngredientEntity,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val lifespan = _getRelativeLifespanFromString(ingredient.expirationDate)
    val dotColor = expiryColor(lifespan)
    val expiryText = expiryLabel(lifespan)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category initial circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ingredient.category.firstOrNull()?.uppercase() ?: "?",
                    fontSize = scaledSp(18f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            // Name + details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ingredient.name,
                    fontSize = scaledSp(16f),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${ingredient.quantity} ${ingredient.unit} · ${ingredient.category}",
                    fontSize = scaledSp(13f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Text(
                        expiryText,
                        fontSize = scaledSp(11f),
                        color = dotColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // ─── Action Buttons — Box+clickable avoids IconButton overlap ─
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Create, "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFEBEB))
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Delete, "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Ingredient Dialog ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDialog(
    title: String,
    userId: String,
    ingredient: IngredientEntity? = null,
    onDismiss: () -> Unit,
    onSave: (IngredientEntity) -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var category by remember { mutableStateOf(ingredient?.category ?: "Vegetable") }
    var quantity by remember { mutableStateOf(ingredient?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "g") }
    var expirationDate by remember { mutableStateOf(ingredient?.expirationDate ?: "") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf("") }
    var quantityError by remember { mutableStateOf("") }

    val categories = listOf("Protein", "Meat", "Dairy", "Condiment", "Grain", "Fruit", "Vegetable")
    val units = listOf("g", "kg", "ml", "l", "pieces", "tbsp", "tsp", "cups")
    val canSave = name.isNotBlank() && quantity.isNotBlank()

    // Date picker shown outside the Dialog so it renders on top
    if (showDatePicker) {
        IngredientDatePickerDialog(
            initialDateString = expirationDate,
            onDateSelected = { selected ->
                expirationDate = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        fontSize = scaledSp(20f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Outlined.Close, "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = "" },
                    label = { Text("Ingredient Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError.isNotEmpty(),
                    supportingText = if (nameError.isNotEmpty()) {
                        { Text(nameError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; showCategoryMenu = false }
                            )
                        }
                    }
                }

                // Quantity + Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it; quantityError = "" },
                        label = { Text("Quantity *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = quantityError.isNotEmpty(),
                        supportingText = if (quantityError.isNotEmpty()) {
                            { Text(quantityError, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                    ExposedDropdownMenuBox(
                        expanded = showUnitMenu,
                        onExpandedChange = { showUnitMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit, onValueChange = {}, readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = { unit = u; showUnitMenu = false }
                                )
                            }
                        }
                    }
                }

                // ─── Expiry Date — Calendar Picker ────────────────────────
                Column {
                    Text(
                        "Expiration Date (optional)",
                        fontSize = scaledSp(12f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (expirationDate.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Outlined.DateRange, null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (expirationDate.isNotBlank()) expirationDate
                            else "Select expiry date",
                            fontSize = scaledSp(14f),
                            modifier = Modifier.weight(1f)
                        )
                        // Clear button if date is set
                        if (expirationDate.isNotBlank()) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.Close, "Clear date",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { expirationDate = "" }
                            )
                        }
                    }
                    Text(
                        "Leave blank if no expiry",
                        fontSize = scaledSp(11f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Text("Cancel") }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                return@Button
                            }
                            if (quantity.isBlank()) {
                                quantityError = "Quantity is required"
                                return@Button
                            }
                            val quantityCapture = regexNumeric.find(quantity)
                            if (quantityCapture == null) {
                                quantityError = "Enter a valid number"
                                return@Button
                            }
                            val realQuantity = quantityCapture.groups["number"]!!.value.toDouble()
                            if (realQuantity < 0) {
                                quantityError = "Must be 0 or greater"
                                return@Button
                            }
                            onSave(
                                IngredientEntity(
                                    userId = userId,
                                    name = name.trim(),
                                    category = category,
                                    quantity = realQuantity,
                                    unit = unit,
                                    expirationDate = expirationDate.trim()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Text("Save") }
                }
            }
        }
    }
}

// ─── Date Picker Dialog ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDatePickerDialog(
    initialDateString: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    val initialMillis = if (initialDateString.isNotBlank()) {
        try {
            LocalDate.parse(initialDateString, formatter)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) { null }
    } else null

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val selected = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selected.format(formatter))
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Text("Cancel") }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ─── Filter Dialog ────────────────────────────────────────────────────────────
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
                    Text(
                        "Filter by Category",
                        fontSize = scaledSp(20f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Outlined.Close, "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "Select a category to filter:",
                    fontSize = scaledSp(14f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onCategorySelect(cat) },
                            label = { Text(cat) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                if (selectedCategory != null) {
                    OutlinedButton(
                        onClick = { onCategorySelect(null); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Text("Clear Filter") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Ingredit() {
    IngredientDialog("Sample", "", null, {}, {})
}