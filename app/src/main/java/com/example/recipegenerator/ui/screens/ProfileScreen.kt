package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.recipegenerator.ui.theme.Brown30
import com.example.recipegenerator.ui.theme.Brown50
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme
import com.example.recipegenerator.ui.theme.SurfaceWhite
import com.example.recipegenerator.ui.viewmodel.ProfileViewModel
import com.example.recipegenerator.ui.viewmodel.ThemeViewModel
import com.example.recipegenerator.ui.theme.scaledSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    padding: PaddingValues = PaddingValues(),
    profileViewModel: ProfileViewModel? = null,
    themeViewModel: ThemeViewModel? = null,
    onBackClick: () -> Unit = {},
    onLogOutClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAppSettingsClick: () -> Unit = {}
) {
    val userState by profileViewModel?.userState?.collectAsState()
        ?: remember { mutableStateOf(null) }
    val user = userState
    val isDarkMode by themeViewModel?.isDarkMode?.collectAsState()
        ?: remember { mutableStateOf(false) }

    val displayName = user?.let { "${it.firstName} ${it.lastName}" } ?: "Loading..."
    val displayUsername = user?.username?.let { "@$it" } ?: ""
    val displayFName = user?.firstName ?: "..."
    val displayLName = user?.lastName ?: "..."
    val displayUser = user?.username ?: "..."
    val displayEmail = user?.email ?: "..."

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDietaryDialog by remember { mutableStateOf(false) }
    var selectedDietaryRestrictions by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ─── Profile Header ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (user != null)
                                "${user.firstName.firstOrNull() ?: ""}${user.lastName.firstOrNull() ?: ""}".uppercase()
                            else "?",
                            fontSize = scaledSp(22f),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            displayName, fontSize = scaledSp(18f), fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (displayUsername.isNotEmpty()) {
                            Text(
                                displayUsername, fontSize = scaledSp(13f),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            displayEmail, fontSize = scaledSp(13f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))

            // ─── Menu List ────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Account section
                item { SectionLabel("Account") }
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Outlined.AccountCircle,
                            title = "Account Settings",
                            subtitle = "Edit your name and username",
                            onClick = { showEditProfileDialog = true }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        ProfileMenuItem(
                            icon = Icons.Outlined.Lock,
                            title = "Change Password",
                            subtitle = "Update your password",
                            onClick = { showChangePasswordDialog = true }
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                // Preferences section
                item { SectionLabel("Preferences") }
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Outlined.Warning,
                            title = "Dietary Restrictions",
                            subtitle = if (selectedDietaryRestrictions.isEmpty()) "No restrictions set"
                            else "${selectedDietaryRestrictions.size} active",
                            onClick = { showDietaryDialog = true },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (selectedDietaryRestrictions.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "${selectedDietaryRestrictions.size}",
                                                fontSize = scaledSp(11f),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Outlined.ArrowForward, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        ProfileMenuToggle(
                            icon = Icons.Outlined.Info,
                            title = "Night Mode",
                            subtitle = if (isDarkMode) "Dark appearance on" else "Light appearance on",
                            checked = isDarkMode,
                            onCheckedChange = { themeViewModel?.setDarkMode(it) }
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                // App section
                item { SectionLabel("App") }
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Outlined.Settings,
                            title = "Application Settings",
                            subtitle = "Font size, language, cache",
                            onClick = { onAppSettingsClick() }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        ProfileMenuItem(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications & Alerts",
                            subtitle = "View expiring ingredient alerts",
                            onClick = { onNotificationsClick() }
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            // ─── Log Out ──────────────────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Button(
                    onClick = onLogOutClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.ExitToApp, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontSize = scaledSp(16f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentFName = displayFName, currentLName = displayLName,
            currentUser = displayUser,
            onDismiss = { showEditProfileDialog = false },
            onSave = { first, last, u ->
                profileViewModel?.updateProfile(first, last, u)
                showEditProfileDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { current, new ->
                profileViewModel?.changePassword(current, new)
                showChangePasswordDialog = false
            }
        )
    }

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
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = scaledSp(11f),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, fontSize = scaledSp(15f), fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle, fontSize = scaledSp(12f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, fontSize = scaledSp(15f), fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle, fontSize = scaledSp(12f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun EditProfileDialog(
    currentFName: String, currentLName: String,
    currentUser: String,
    onDismiss: () -> Unit, onSave: (String, String, String) -> Unit
) {
    var fName by remember { mutableStateOf(currentFName) }
    var lName by remember { mutableStateOf(currentLName) }
    var user by remember { mutableStateOf(currentUser) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Profile", fontSize = scaledSp(20f), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedTextField(value = fName, onValueChange = { fName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Person, "Name", tint = MaterialTheme.colorScheme.primary) })
                OutlinedTextField(value = lName, onValueChange = { lName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Person, "Name", tint = MaterialTheme.colorScheme.primary) })
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Person, "Name", tint = MaterialTheme.colorScheme.primary) })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text("Cancel") }
                    Button(onClick = { onSave(fName, lName, user) }, modifier = Modifier.weight(1f), enabled = fName.isNotBlank() && lName.isNotBlank() && user.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Change Password", fontSize = scaledSp(20f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("Current Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) { Icon(if (showCurrentPassword) Icons.Outlined.Face else Icons.Outlined.Lock, "Toggle", tint = MaterialTheme.colorScheme.primary) } })
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showNewPassword = !showNewPassword }) { Icon(if (showNewPassword) Icons.Outlined.Face else Icons.Outlined.Lock, "Toggle", tint = MaterialTheme.colorScheme.primary) } })
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm New Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) { Icon(if (showConfirmPassword) Icons.Outlined.Face else Icons.Outlined.Lock, "Toggle", tint = MaterialTheme.colorScheme.primary) } }, isError = confirmPassword.isNotBlank() && !passwordsMatch, supportingText = { if (confirmPassword.isNotBlank() && !passwordsMatch) Text("Passwords don't match", color = MaterialTheme.colorScheme.error) })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text("Cancel") }
                    Button(onClick = { onSave(currentPassword, newPassword) }, modifier = Modifier.weight(1f), enabled = currentPassword.isNotBlank() && passwordsMatch, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Change") }
                }
            }
        }
    }
}

@Composable
fun DietaryRestrictionsDialog(selectedRestrictions: Set<String>, onDismiss: () -> Unit, onSave: (Set<String>) -> Unit) {
    var restrictions by remember { mutableStateOf(selectedRestrictions) }
    val availableRestrictions = listOf("Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Nut Allergy", "Shellfish Allergy", "Egg Allergy", "Soy Allergy", "Halal", "Kosher", "Low-Carb", "Keto", "Paleo")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Dietary Restrictions", fontSize = scaledSp(20f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Text("Select all that apply:", fontSize = scaledSp(14f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableRestrictions.chunked(2).forEach { rowRestrictions ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowRestrictions.forEach { restriction ->
                                FilterChip(selected = restriction in restrictions, onClick = { restrictions = if (restriction in restrictions) restrictions - restriction else restrictions + restriction }, label = { Text(restriction) }, modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, labelColor = MaterialTheme.colorScheme.onSurface, selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                            }
                            if (rowRestrictions.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { restrictions = emptySet() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)) { Text("Clear All") }
                    Button(onClick = { onSave(restrictions) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProfileScreenPreview() {
    RecipeGeneratorTheme(darkTheme = false, dynamicColor = false) {
        ProfileScreen()
    }
}