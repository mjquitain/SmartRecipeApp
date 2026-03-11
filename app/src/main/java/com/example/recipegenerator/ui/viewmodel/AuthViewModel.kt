package com.example.recipegenerator.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.dao.UserDao
import com.example.recipegenerator.data.entity.UserEntity
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // Sealed class so MainActivity knows exactly what happened
    sealed class AuthResult {
        object Success : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    /**
     * Sign In — looks up user in Room, validates password
     */
    fun signIn(username: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)

            when {
                user == null -> {
                    _authResult.postValue(AuthResult.Error("Username not found"))
                }
                user.password != password -> {
                    _authResult.postValue(AuthResult.Error("Incorrect password"))
                }
                else -> {
                    // Save who is currently logged in
                    sharedPreferences.edit()
                        .putString("current_username", username)
                        .apply()

                    if (rememberMe) {
                        sharedPreferences.edit()
                            .putString("saved_user", username)
                            .putString("saved_pass", password)
                            .putBoolean("is_remembered", true)
                            .apply()
                    } else {
                        sharedPreferences.edit()
                            .remove("saved_user")
                            .remove("saved_pass")
                            .putBoolean("is_remembered", false)
                            .apply()
                    }

                    _authResult.postValue(AuthResult.Success)
                }
            }
        }
    }

    /**
     * Sign Up — checks for duplicate username, then inserts into Room
     */
    fun signUp(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            // Check if username already taken
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                _authResult.postValue(AuthResult.Error("Username '$username' is already taken"))
                return@launch
            }

            try {
                userDao.registerUser(
                    UserEntity(
                        username = username,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password
                    )
                )

                // Auto-login after successful registration
                sharedPreferences.edit()
                    .putString("current_username", username)
                    .apply()

                _authResult.postValue(AuthResult.Success)

            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error("Registration failed: ${e.message}"))
            }
        }
    }
}

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val sharedPrefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}