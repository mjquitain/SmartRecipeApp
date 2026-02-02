package com.example.recipegenerator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.ui.components.MinimalListItem
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme
import java.time.Clock
import java.time.LocalDate



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsListPage(
    padding : PaddingValues = PaddingValues(),
    onBackClick : () -> Unit = {},
    onAddClick : () -> Unit = {},
    onFilterClick : () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Ingredients") },
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
        val listScroller = rememberScrollableState { 0.0f }

        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .padding(16.dp, 20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val autoWeight = Modifier.weight(1f)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%d ingredients",
                        fontSize = 20.sp,
                        modifier = autoWeight
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shrunkenButtonModifier = Modifier
                                .padding(0.dp)
                                .size(30.dp)
                        IconButton(
                            modifier = shrunkenButtonModifier,
                            onClick = onAddClick
                        ) {
                            Icon(
                                imageVector = Icons.Sharp.Add,
                                contentDescription = null
                            )
                        }
                        IconButton(
                            modifier = shrunkenButtonModifier,
                            onClick = onFilterClick
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = ""
                            )
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .scrollable(state = listScroller, orientation = Orientation.Vertical),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..10) {
                        item { IngredientListItem() }
                    }
                }
            }
        }
    }
}



@Composable
fun IngredientListItem(
    title : String = "Ingredient",
    onTitleClick : () -> Unit = {},
    onLifetimeClick : (LocalDate) -> Unit = {},
    onEditClick : (/* insert value types here when we can store ingredients now */) -> Unit = {},
    onDeleteClick : (/* insert value types here when we can store ingredients now */) -> Unit = {}
) {
    val containerTitleFontSize = 16.sp

    val selectableContainerAlignment = Alignment.CenterVertically

    val titleContainer = Modifier
        .fillMaxSize()
        .padding(16.dp, 12.dp)
        .clickable(
            enabled = true,
            onClick = onTitleClick
        )

    val actionsContainer = Modifier
        .defaultMinSize(100.dp)
        .width(100.dp)
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.primary)

//    Box(modifier = selectableContainerModifier) {
//
//    }

    MinimalListItem() {
        Row(modifier = Modifier.fillMaxSize()) {
            val autoWeight = Modifier.weight(1f)

            Row(
                modifier = titleContainer.weight(1.0f),
                verticalAlignment = selectableContainerAlignment,
            ) {
                Text("Oreo McFlurry",
                    modifier = autoWeight,
                    fontSize = containerTitleFontSize,
                )
                Spacer(Modifier.width(10.dp))
                Box(Modifier
                    .size(20.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(0.3f, 0.9f, 0.1f))
                    .clickable(
                        enabled = true,
                        // TODO: This should present the expiration date instead.
                        //   When information about ingredients are finally
                        //   storable, replace this with the actual date.
                        //   .
                        //   Also, the code below is supported only for API version
                        //   26 (= to Android 7 [Nougat]) and the project is set to
                        //   use at least API 23 (= to Android 6 [Marshmallow]).
                        onClick = {onLifetimeClick(LocalDate.now(Clock.systemDefaultZone()))}
                    )
                )
            }
            Box(modifier = actionsContainer) {
                Row(
                    verticalAlignment = selectableContainerAlignment
                ) {
                    IconButton(onClick = {}, modifier = autoWeight) {
                        Icon(
                            imageVector = Icons.Outlined.Create,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = {}, modifier = autoWeight) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}



@Composable
@Preview(showBackground = true)
fun IngredientsListPagePreview() {
    RecipeGeneratorTheme(dynamicColor = false) {
        IngredientsListPage()
    }
}