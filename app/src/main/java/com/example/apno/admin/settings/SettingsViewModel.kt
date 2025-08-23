package com.example.apno.admin.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apno.data.model.Settings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val settingsDoc = db.collection("settings").document("main")

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings = _settings.asStateFlow()

    init {
        listenForSettingsChanges()
    }

    private fun listenForSettingsChanges() {
        settingsDoc.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _settings.value = snapshot?.toObject(Settings::class.java)
        }
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            try {
                settingsDoc.set(settings).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
