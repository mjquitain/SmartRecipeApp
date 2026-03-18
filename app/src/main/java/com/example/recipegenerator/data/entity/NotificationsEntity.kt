package com.example.recipegenerator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ingredientName: String,
    val message: String,
    val daysUntilExpiry: Long,
    val isCritical: Boolean, // true if expiring within 3 days
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)