package com.example.apno.admin.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apno.R
import com.example.apno.data.model.Settings

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()
    val settings by viewModel.settings.collectAsState()

    settings?.let { currentSettings ->
        var storeName by remember { mutableStateOf(currentSettings.storeName) }
        var address by remember { mutableStateOf(currentSettings.address) }
        var ruc by remember { mutableStateOf(currentSettings.ruc) }
        var currency by remember { mutableStateOf(currentSettings.currency) }
        var taxPercent by remember { mutableStateOf(currentSettings.taxPercent.toString()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text(stringResource(R.string.store_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.address)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ruc,
                onValueChange = { ruc = it },
                label = { Text(stringResource(R.string.tax_id_ruc)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text(stringResource(R.string.currency)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = taxPercent,
                onValueChange = { taxPercent = it },
                label = { Text(stringResource(R.string.tax_percent)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val updatedSettings = currentSettings.copy(
                        storeName = storeName,
                        address = address,
                        ruc = ruc,
                        currency = currency,
                        taxPercent = taxPercent.toDoubleOrNull() ?: 0.0
                    )
                    viewModel.saveSettings(updatedSettings)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_settings))
            }
        }
    }
}
