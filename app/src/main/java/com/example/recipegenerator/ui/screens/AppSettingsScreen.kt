package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.ui.theme.Brown30
import com.example.recipegenerator.ui.theme.Brown50
import com.example.recipegenerator.ui.theme.ChipBackground
import com.example.recipegenerator.ui.theme.SurfaceWhite
import com.example.recipegenerator.ui.theme.TextPrimary
import com.example.recipegenerator.ui.viewmodel.AppSettingsViewModel
import com.example.recipegenerator.ui.theme.scaledSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    padding: PaddingValues = PaddingValues(),
    appSettingsViewModel: AppSettingsViewModel,
    onBackClick: () -> Unit = {}
) {
    val settings by appSettingsViewModel.settings.collectAsState()

    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ─── Appearance ───────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                SettingsSectionLabel("Appearance")
            }

            // Font Size
            item {
                SettingsSelectorItem(
                    icon = Icons.Outlined.Create,
                    title = "Font Size",
                    subtitle = "Current: ${settings.fontSize}",
                    options = listOf("Small", "Medium", "Large"),
                    selected = settings.fontSize,
                    onSelect = { appSettingsViewModel.setFontSize(it) }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            // ─── Language ─────────────────────────────────────────────────
            item { SettingsSectionLabel("Language") }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Settings, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "App Language",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "English",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // "Coming soon" badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "More coming soon",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(4.dp)) }

            // ─── Data ─────────────────────────────────────────────────────
            item { SettingsSectionLabel("Data") }

            item {
                SettingsClickItem(
                    icon = Icons.Outlined.Delete,
                    title = "Clear App Data",
                    subtitle = "Remove cached data and preferences",
                    iconTint = Color(0xFFE53935),
                    onClick = { showClearCacheDialog = true }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            // ─── About ────────────────────────────────────────────────────
            item { SettingsSectionLabel("About") }

            item {
                SettingsClickItem(
                    icon = Icons.Outlined.Info,
                    title = "About the App",
                    subtitle = "Version info and credits",
                    onClick = { showAboutDialog = true }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Clear App Data?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "This will clear cached data and reset preferences. Your ingredients and recipes will not be deleted.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showClearCacheDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = SurfaceWhite
                    )
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCacheDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Brown50
                    )
                ) { Text("Cancel") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "About Castelli Recipes",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Version 1.0.0",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "A smart recipe generator that helps you find delicious meals based on ingredients you already have.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = scaledSp(13f)
                    )
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "• Jetpack Compose",
                        "• Room Database",
                        "• TheMealDB API",
                        "• Firebase"
                    ).forEach {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = scaledSp(12f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown50,
                        contentColor = SurfaceWhite
                    )
                ) { Text("Close") }
            }
        )
    }
}

// ─── Reusable Settings Components ────────────────────────────────────────────

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        title.uppercase(),
        fontSize = scaledSp(11f),
        fontWeight = FontWeight.Bold,
        color = Brown30,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsSelectorItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingsIconBox(icon, Brown50)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        title,
                        fontSize = scaledSp(15f),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        subtitle,
                        fontSize = scaledSp(12f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(option) },
                        label = { Text(option, fontSize = scaledSp(13f)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = Brown50,
                            selectedLabelColor = SurfaceWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected == option,
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = Brown50,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBox(icon, iconTint)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = scaledSp(15f),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    fontSize = scaledSp(12f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Outlined.KeyboardArrowRight,
                null,
                tint = Brown30,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsIconBox(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
    }
}