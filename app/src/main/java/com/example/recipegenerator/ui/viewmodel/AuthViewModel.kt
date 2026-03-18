package com.example.recipegenerator.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipegenerator.data.dao.UserDao
import com.example.recipegenerator.data.entity.UserEntity
import com.example.recipegenerator.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.rpc.context.AttributeContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: UserRepository,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    sealed class AuthResult {
        object Success : AuthResult()
        object NeedsVerification : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun signIn(email: String, pass: String, rememberMe: Boolean) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {

                        sharedPreferences.edit()
                            .putBoolean("is_remembered", rememberMe)
                            .putString("saved_user", if (rememberMe) email else "")
                            .putString("current_username", user.uid)
                            .apply()

                        _authResult.postValue(AuthResult.Success)
                    } else {
                        auth.signOut()
                        _authResult.postValue(AuthResult.Error("Please verify your email address before signing in."))
                    }
                } else {
                    _authResult.postValue(AuthResult.Error(task.exception?.message ?: "Invalid Email or Password"))
                }
            }
    }

    fun signUp(fName: String, lName: String, uname: String, email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email,pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: ""

                    val entity = UserEntity(
                        uid = uid,
                        username = uname,
                        firstName = fName,
                        lastName = lName,
                        email = email
                    )

                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            viewModelScope.launch {
                                repository.createOrUpdateUser(entity)
                                auth.signOut()
                                _authResult.postValue(AuthResult.NeedsVerification)
                            }
                        }
                    }
                } else {
                    val exception = task.exception
                    val errorMsg = when (exception) {
                        is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                            "This email is already registered. Please sign in instead."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            "The email address is badly formatted."
                        else -> exception?.message ?: "Sign Up Failed"
                    }
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            }
    }

    fun handleGoogleSignIn(firebaseUser: FirebaseUser) {
        val entity = UserEntity(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            username = firebaseUser.displayName ?: "",
            firstName = firebaseUser.displayName?.split(" ")?.getOrNull(0) ?: "",
            lastName = firebaseUser.displayName?.split(" ")?.getOrNull(1) ?: ""
        )

        viewModelScope.launch{
            repository.createOrUpdateUser(entity)

            sharedPreferences.edit()
                .putString("crrent_username", firebaseUser.uid)
                .apply()

            _authResult.postValue(AuthResult.Success)
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                _snackbarMessage.emit("Reset email sent! Check your inbox.")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error: ${e.message}")
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
            val repository = UserRepository(userDao)

            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}