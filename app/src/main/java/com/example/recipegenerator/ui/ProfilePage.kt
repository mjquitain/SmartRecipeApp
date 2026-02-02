package com.example.recipegenerator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.sharp.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme



// TODO: Maybe store this in a "namespace" object? At least both the object and this function can
//   be linked in navigation nodes. Maybe like "ProfileNavNode.ProfilePage()"?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    padding : PaddingValues = PaddingValues(),
    onBackClick : () -> Unit = {},
    onLogOutClick : () -> Unit = {}, // Passed to all notifications. Only after being able to store notification messages temporarily will this be relevant.
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
                // NOTE: Removed shadows for IngredientsListPage.kt, NotificationsPage.kt, and
                //   ProfilePage.kt to conform to the appearance of other pages.
//                modifier = Modifier
//                    .shadow(10.dp)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(20.dp)
            ) {
                Row() {
                    // TODO: Swap with image when uploading is available?
                    Icon(
                        imageVector = Icons.Sharp.AccountCircle,
                        contentDescription = "profile",
                        modifier = Modifier
                            .aspectRatio(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Gordon", fontSize = 20.sp)
                        Text("abc@def.com")
                    }
                }
            }
            HorizontalDivider()
            // TODO: Somehow make it easy to listen to the click events of these options.
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {ListItem(
                    headlineContent = {Text("Account Settings")},
                    leadingContent = {Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null)},
                    trailingContent = {Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)}
                )}
                item {ListItem(
                    headlineContent = {Text("Dietary Restrictions")},
                    leadingContent = {Icon(imageVector = Icons.Outlined.Warning, contentDescription = null)},
                    trailingContent = {Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)}
                )}
                item {ListItem(
                    headlineContent = {Text("Application Settings")},
                    leadingContent = {Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)},
                    trailingContent = {Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)}
                )}
                item {ListItem(
                    headlineContent = {Text("Notifications & Alerts")},
                    leadingContent = {Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null)},
                    trailingContent = {Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)}
                )}
                /* TODO: Should this be inside an "Appearance Settings" page instead?
                     Also, probably better if the icon is other than a generic Info.
                     Install that one package with extra icons.*/
                item {ListItem(
                    headlineContent = {Text("Use Night Mode")},
                    leadingContent = {Icon(imageVector = Icons.Outlined.Info, contentDescription = null)},
                    trailingContent = {Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)}
                )}
            }
            HorizontalDivider()
            Box(Modifier.fillMaxWidth().padding(10.dp)) {
                Button(onLogOutClick, modifier = Modifier.fillMaxWidth()) {Text("Log out")}
            }
        }
    }
}



@Composable
@Preview(showBackground = true)
fun ProfilePagePreview() {
    RecipeGeneratorTheme(darkTheme = false, dynamicColor = false) {
        ProfilePage()
    }
}