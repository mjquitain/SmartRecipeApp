package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.sharp.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.recipegenerator.navigation.SettingsGraph
import com.example.recipegenerator.ui.components.OptionItem
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme
import com.example.recipegenerator.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    padding: PaddingValues = PaddingValues(),
    navController: NavController,
    profileViewModel: ProfileViewModel? = null,
    onBackClick: () -> Unit = {},
    onLogOutClick: () -> Unit = {}
) {
    val userState = profileViewModel?.userState?.collectAsState()
    val user = userState?.value

    val displayName = user?.let { "${it.firstName} ${it.lastName} (${it.username})" } ?: "Loading..."
    val displayFName = user?.firstName ?: "..."
    val displayLName = user?.lastName ?: "..."
    val displayUser = user?.username ?: "..."
    val displayEmail = user?.email ?: "..."

    // Dialog states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDietaryDialog by remember { mutableStateOf(false) }

    // Settings states
    var nightModeEnabled by remember { mutableStateOf(false) }

    // Dietary restrictions
    var selectedDietaryRestrictions by remember {
        mutableStateOf(setOf<String>())
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Profile Header - Clickable to edit
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(20.dp)
                    .clickable { showEditProfileDialog = true }
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Sharp.AccountCircle,
                        contentDescription = "profile",
                        modifier = Modifier.aspectRatio(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(displayName, fontSize = 20.sp)
                        Text(displayEmail)
                    }
                }
            }
            HorizontalDivider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                // Account Settings - opens edit dialog
                item {
                    OptionItem(
                        title = "Account Settings",
                        icon = Icons.Outlined.AccountCircle,
                        onClick = { showEditProfileDialog = true }
                    )
                }

                // Change Password
                item {
                    OptionItem(
                        title = "Change Password",
                        icon = Icons.Outlined.Lock,
                        onClick = { showChangePasswordDialog = true }
                    )
                }

                // Dietary Restrictions
                item {
                    OptionItem(
                        title = "Dietary Restrictions",
                        icon = Icons.Outlined.Warning,
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedDietaryRestrictions.isNotEmpty()) {
                                    Text(
                                        "${selectedDietaryRestrictions.size} active",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        },
                        onClick = { showDietaryDialog = true }
                    )
                }

                // Application Settings - placeholder
                item {
                    OptionItem(
                        title = "Application Settings",
                        icon = Icons.Outlined.Settings,
                        navController = navController,
                        destination = null
                    )
                }

                // Notifications & Alerts - navigate to notifications screen
                item {
                    OptionItem(
                        title = "Notifications & Alerts",
                        icon = Icons.Outlined.Settings,
                        navController = navController,
                        destination = SettingsGraph.NotificationsNode
                    )
                }

                // Night Mode Toggle
                item {
                    ListItem(
                        headlineContent = { Text("Use Night Mode") },
                        leadingContent = {
                            Icon(Icons.Outlined.Info, "Night Mode")
                        },
                        trailingContent = {
                            Switch(
                                checked = nightModeEnabled,
                                onCheckedChange = { nightModeEnabled = it }
                            )
                        }
                    )
                }
            }

            HorizontalDivider()
            Box(Modifier.fillMaxWidth().padding(10.dp)) {
                Button(onLogOutClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Log out")
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentFName = displayFName,
            currentLName = displayLName,
            currentUser = displayUser,
            currentEmail = displayEmail,
            onDismiss = { showEditProfileDialog = false },
            onSave = { first, last, user, email ->
                profileViewModel?.updateProfile(first, last, user, email)
                showEditProfileDialog = false
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = {
                showChangePasswordDialog = false
                // TODO: Implement password change logic
            }
        )
    }

    // Dietary Restrictions Dialog
    if (showDietaryDialog) {
        DietaryRestrictionsDialog(
            selectedRestrictions = selectedDietaryRestrictions,
            onDismiss = { showDietaryDialog = false },
            onSave = { restrictions ->
                selectedDietaryRestrictions = restrictions
                showDietaryDialog = false
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    currentFName: String,
    currentLName: String,
    currentUser: String,
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var fName by remember { mutableStateOf(currentFName) }
    var lName by remember { mutableStateOf(currentLName) }
    var user by remember { mutableStateOf(currentUser) }
    var email by remember { mutableStateOf(currentEmail) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, "Close")
                    }
                }

                OutlinedTextField(
                    value = fName,
                    onValueChange = { fName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Person, "Name") }
                )

                OutlinedTextField(
                    value = lName,
                    onValueChange = { lName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Person, "Name") }
                )

                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Person, "Name") }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Email, "Email") }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(fName, lName, user, email) },
                        modifier = Modifier.weight(1f),
                        enabled = fName.isNotBlank() && lName.isNotBlank() && user.isNotBlank() && email.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotBlank()

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
                    Text("Change Password", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, "Close")
                    }
                }

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                if (showCurrentPassword) Icons.Outlined.Face else Icons.Outlined.Lock,
                                "Toggle password visibility"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                if (showNewPassword) Icons.Outlined.Face else Icons.Outlined.Lock,
                                "Toggle password visibility"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Outlined.Face else Icons.Outlined.Lock,
                                "Toggle password visibility"
                            )
                        }
                    },
                    isError = confirmPassword.isNotBlank() && !passwordsMatch,
                    supportingText = {
                        if (confirmPassword.isNotBlank() && !passwordsMatch) {
                            Text("Passwords don't match", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = currentPassword.isNotBlank() && passwordsMatch
                    ) {
                        Text("Change")
                    }
                }
            }
        }
    }
}

@Composable
fun DietaryRestrictionsDialog(
    selectedRestrictions: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var restrictions by remember { mutableStateOf(selectedRestrictions) }

    val availableRestrictions = listOf(
        "Vegetarian",
        "Vegan",
        "Gluten-Free",
        "Dairy-Free",
        "Nut Allergy",
        "Shellfish Allergy",
        "Egg Allergy",
        "Soy Allergy",
        "Halal",
        "Kosher",
        "Low-Carb",
        "Keto",
        "Paleo"
    )

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
                    Text("Dietary Restrictions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, "Close")
                    }
                }

                Text(
                    "Select all that apply:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableRestrictions.chunked(2).forEach { rowRestrictions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowRestrictions.forEach { restriction ->
                                FilterChip(
                                    selected = restriction in restrictions,
                                    onClick = {
                                        restrictions = if (restriction in restrictions) {
                                            restrictions - restriction
                                        } else {
                                            restrictions + restriction
                                        }
                                    },
                                    label = { Text(restriction) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Add spacer if odd number
                            if (rowRestrictions.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            restrictions = emptySet()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All")
                    }
                    Button(
                        onClick = { onSave(restrictions) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProfileScreenPreview() {
    RecipeGeneratorTheme(darkTheme = false, dynamicColor = false) {
        ProfileScreen(navController = rememberNavController())
    }
}