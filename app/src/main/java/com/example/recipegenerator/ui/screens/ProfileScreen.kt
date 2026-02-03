package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.recipegenerator.navigation.SettingsGraph
import com.example.recipegenerator.ui.components.OptionItem
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    padding : PaddingValues = PaddingValues(),
    navController : NavController,
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
                // NOTE: Removed shadows for IngredientsListScreen.kt, NotificationsScreen.kt, and
                //   ProfileScreen.kt to conform to the appearance of other pages.
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
                item { OptionItem(
                    "Account Settings",
                    Icons.Outlined.AccountCircle,
                    navController, null
                ) }// SettingsGraph.NotificationsNode

                item { OptionItem(
                    "Dietary Restrictions",
                    Icons.Outlined.Warning,
                    navController, null
                ) }

                item { OptionItem(
                    "Application Settings",
                    Icons.Outlined.Settings,
                    navController, null
                ) }

                item { OptionItem(
                    "Notifications & Alerts",
                    Icons.Outlined.Settings,
                    navController, SettingsGraph.NotificationsNode
                ) }

                /* TODO: Should this be inside an "Appearance Settings" page instead?
                     Also, probably better if the icon is other than a generic Info.
                     Install that one package with extra icons.*/

                item { OptionItem(
                    "Use Night Mode",
                    Icons.Outlined.Info,
                    {/* TODO: Implement night mode here */},
                ) {/* TODO: Implement toggle here */} }
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
fun ProfileScreenPreview() {
    RecipeGeneratorTheme(darkTheme = false, dynamicColor = false) {
        ProfileScreen(navController = rememberNavController())
    }
}