package com.example.recipegenerator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    var uid: String = "",
    var username: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this ("", "", "","", "")
}