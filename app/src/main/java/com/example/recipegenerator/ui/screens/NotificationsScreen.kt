package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    padding : PaddingValues = PaddingValues(),
    onBackClick : () -> Unit = {},
    onClearAllClick : () -> Unit = {}, // Only after being able to store notification messages temporarily will this be relevant.
    onRemoveNotificationClick : () -> Unit = {}, // Passed to all notifications. Only after being able to store notification messages temporarily will this be relevant.
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                // NOTE: Removed shadows for IngredientsListScreen.kt, NotificationsScreen.kt, and
                //   ProfileScreen.kt to conform to the appearance of other pages.
//                modifier = Modifier
//                    .shadow(10.dp)
            )
        },
    ) {
        Column(modifier = Modifier.padding(it)) {
            Row(modifier = Modifier.padding(10.dp)) {
            TextButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Clear all")
                }
            }
            LazyColumn() {
                for (i in 1..5) {
                    item {
                        HorizontalDivider()
                        ListItem(
                            // TODO: When we can temp store notifications, make these reflect their information.
                            headlineContent = {Text("Sample notification")},
                            trailingContent = {Text("01/01/1970, 12:00AM")},
                            leadingContent = {Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null
                            )},
                            overlineContent = {Text("Information")},
                            supportingContent = {Text("This is a sample summary.")},
                        )
                    }
                }

                item {HorizontalDivider()}
            }
        }
    }
}



@Composable
@Preview(showBackground = true)
fun NotificationsScreenPreview() {
    RecipeGeneratorTheme(
        darkTheme = false,
        dynamicColor = false,
    ) {
        NotificationsScreen()
    }
}