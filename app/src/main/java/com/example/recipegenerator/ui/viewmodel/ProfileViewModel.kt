package com.example.recipegenerator.ui.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.dao.UserDao
import com.example.recipegenerator.data.entity.UserEntity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _userState = MutableStateFlow<UserEntity?>(null)
    val userState: StateFlow<UserEntity?> = _userState

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        observeFirestoreUser()
    }

    private fun observeFirestoreUser() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                try {
                    val user = snapshot.toObject(UserEntity::class.java)

                    if (user != null) {
                        val finalUser = user.copy(uid = snapshot.id)

                        viewModelScope.launch {
                            _userState.value = finalUser
                            userDao.insertUser(finalUser)
                            sharedPreferences.edit()
                                .putString("current_username", finalUser.username)
                                .apply()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirestoreError", "Serialization failed: ${e.message}")
                }
            }
    }

    fun updateProfile(firstName: String, lastName: String, username: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .update(
                        "firstname", firstName,
                        "lastName", lastName,
                        "username", username
                    ).await()

                sharedPreferences.edit()
                    .putString("current_username", username)
                    .apply()

                _snackbarMessage.emit("Profile updated successfully")
            } catch (e: Exception) {
                _snackbarMessage.emit("Update failed: ${e.message}")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val email = user?.email ?: return
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)

        viewModelScope.launch {
            try {
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
                _snackbarMessage.emit("Password updated successsfully!")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: Please log out and back in to change password.")
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val sharedPrefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel:: class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userDao, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}