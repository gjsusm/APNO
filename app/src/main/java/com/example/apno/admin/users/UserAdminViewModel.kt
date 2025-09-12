package com.example.apno.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserAdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    init {
        listenForUserChanges()
    }

    private fun listenForUserChanges() {
        usersCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _users.value = snapshot?.toObjects() ?: emptyList()
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                usersCollection.document(userId).update("role", newRole).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
