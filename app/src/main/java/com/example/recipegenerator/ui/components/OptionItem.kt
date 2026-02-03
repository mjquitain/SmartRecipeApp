package com.example.recipegenerator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController



@Composable
fun <Destination : Any> OptionItem(
    title : String = "Option",
    icon : ImageVector = Icons.Outlined.Info,
    navController : NavController,
    destination : Destination?
) {
    ListItem(
        modifier = Modifier.clickable(
            enabled = true,
            onClick = {if (destination != null) navController.navigate(destination) }
        ),
        headlineContent = {Text(title)},
        leadingContent = {Icon(imageVector = icon, contentDescription = null)},
        trailingContent = {Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)}
    )
}



@Composable
fun OptionItem(
    title : String = "Option",
    icon : ImageVector = Icons.Outlined.Info,
    trailingContent : @Composable () -> Unit = {},
    onClick : () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable(
            enabled = true,
            onClick = onClick
        ),
        headlineContent = {Text(title)},
        leadingContent = {Icon(imageVector = icon, contentDescription = null)},
        trailingContent = trailingContent,
    )
}