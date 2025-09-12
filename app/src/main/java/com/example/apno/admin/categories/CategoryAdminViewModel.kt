package com.example.apno.admin.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryAdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        listenForCategoryChanges()
    }

    private fun listenForCategoryChanges() {
        categoriesCollection.orderBy("order").addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _categories.value = snapshot?.toObjects() ?: emptyList()
        }
    }

    fun addCategory(name: String, order: Int) {
        viewModelScope.launch {
            try {
                val category = Category(name = name, order = order)
                categoriesCollection.add(category).await()
                // No need to fetch, listener will update automatically
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                // Firestore creates the document if it doesn't exist, so we need an ID.
                // This assumes the category object has a valid ID from Firestore.
                // For new categories, Firestore generates an ID after the add operation.
                // The list will be updated by the listener, so we need to make sure
                // the Category objects in our list have their Firestore-generated IDs.
                // The current implementation where we refetch the whole list works around this.
                // A better approach would be to get the ID from the added document and update the local state.
                // But with the snapshot listener, we don't need to do anything manually.
                categoriesCollection.document(category.id).set(category).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoriesCollection.document(categoryId).delete().await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
