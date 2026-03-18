package com.example.recipegenerator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = false,
    val fontSize: String = "Medium", // Small, Medium, Large
    val language: String = "English"
)