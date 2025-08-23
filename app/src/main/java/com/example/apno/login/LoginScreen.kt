package com.example.apno.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { loginViewModel.login(email, password) },
            enabled = loginState != LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        when (loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
            }
            is LoginState.Error -> {
                Text(text = (loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            LoginState.Loading -> {
                CircularProgressIndicator()
            }
            else -> {}
        }
    }
}
