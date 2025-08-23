package com.example.apno.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.user.UserManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    val userDoc = db.collection("users").document(user.uid).get().await()
                    UserManager.currentUser = userDoc.toObject<com.example.apno.data.model.User>()
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Failed to get user.")
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

// Helper function to use await with Task
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return kotlinx.coroutines.tasks.await()
}
