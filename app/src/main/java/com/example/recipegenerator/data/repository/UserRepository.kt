package com.example.recipegenerator.data.repository

import com.example.recipegenerator.data.dao.UserDao
import com.example.recipegenerator.data.entity.UserEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    suspend fun createOrUpdateUser(user: UserEntity) {
        userDao.insertUser(user)

        firestore.collection("users")
            .document(user.uid)
            .set(user, SetOptions.merge())
            .await()
    }
    suspend fun getLocalUser(uid: String): UserEntity? {
        return userDao.getUserById(uid)
    }
}