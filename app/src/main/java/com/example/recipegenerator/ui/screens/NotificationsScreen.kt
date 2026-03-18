package com.example.recipegenerator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipegenerator.data.entity.NotificationEntity
import com.example.recipegenerator.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.recipegenerator.ui.theme.scaledSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    padding: PaddingValues = PaddingValues(),
    notificationViewModel: NotificationViewModel,
    userId: String,
    onBackClick: () -> Unit = {}
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(userId) { notificationViewModel.syncNotifications(userId) }

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(padding),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Notifications",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "$unreadCount", fontSize = scaledSp(11f),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (notifications.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { notificationViewModel.clearAll() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear all", fontSize = scaledSp(13f))
                    }
                }
            }

            if (notifications.isEmpty()) {
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
                                Icons.Outlined.Notifications, null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "No notifications", fontSize = scaledSp(16f),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Ingredients expiring within 7 days will appear here",
                            fontSize = scaledSp(13f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val critical = notifications.filter { it.isCritical }
                val upcoming = notifications.filter { !it.isCritical }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (critical.isNotEmpty()) {
                        item {
                            Text(
                                "URGENT", fontSize = scaledSp(11f), fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935), letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(critical) { notification ->
                            NotificationCard(
                                notification = notification,
                                onRead = { notificationViewModel.markAsRead(notification.id) }
                            )
                        }
                    }

                    if (upcoming.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "UPCOMING", fontSize = scaledSp(11f), fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(upcoming) { notification ->
                            NotificationCard(
                                notification = notification,
                                onRead = { notificationViewModel.markAsRead(notification.id) }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationEntity, onRead: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val timeStr = dateFormat.format(Date(notification.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onRead
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (notification.isCritical) Color(0xFFFFEBEB)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (notification.isCritical) Icons.Outlined.Warning
                    else Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (notification.isCritical) Color(0xFFE53935)
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.message, fontSize = scaledSp(14f),
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(timeStr, fontSize = scaledSp(11f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (notification.isCritical) Color(0xFFE53935)
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}