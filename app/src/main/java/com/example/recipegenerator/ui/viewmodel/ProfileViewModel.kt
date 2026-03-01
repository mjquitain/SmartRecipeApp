package com.example.recipegenerator.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.dao.UserDao
import com.example.recipegenerator.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _userState = MutableStateFlow<UserEntity?>(null)
    val userState: StateFlow<UserEntity?> = _userState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val username = sharedPreferences.getString("current_username", null)
        if (username != null) {
            viewModelScope.launch {
                val userRecord = userDao.getUserByUsername(username)
                _userState.value = userRecord
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, username: String, email: String) {
        val currentUser = _userState.value ?: return
        val updatedUser = currentUser.copy(firstName = firstName, lastName = lastName, username = username, email = email)
        viewModelScope.launch {
            userDao.deleteUser(currentUser)
            userDao.registerUser(updatedUser)
            sharedPreferences.edit()
                .putString("current_username", username)
                .apply()
            _userState.value = updatedUser
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