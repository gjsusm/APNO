package com.example.apno.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.user.UserManager
import com.example.apno.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val userDocRef = db.collection("users").document(firebaseUser.uid)
                    val userDoc = userDocRef.get().await()

                    if (userDoc.exists()) {
                        // User document exists, load it
                        UserManager.currentUser = userDoc.toObject<User>()
                    } else {
                        // First login, create user document
                        val newUser = User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.displayName ?: email,
                            email = email,
                            role = "employee" // Default role
                        )
                        userDocRef.set(newUser).await()
                        UserManager.currentUser = newUser
                    }
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Failed to authenticate user.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
