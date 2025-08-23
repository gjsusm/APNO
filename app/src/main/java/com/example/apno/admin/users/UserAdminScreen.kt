package com.example.apno.admin.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apno.data.model.User

@Composable
fun UserAdminScreen() {
    val viewModel: UserAdminViewModel = viewModel()
    val users by viewModel.users.collectAsState()
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }

    LazyColumn {
        items(users) { user ->
            ListItem(
                headlineText = { Text(user.name) },
                supportingText = { Text(user.email) },
                trailingContent = { Text(user.role) },
                modifier = Modifier.clickable {
                    selectedUser = user
                    showRoleDialog = true
                }
            )
        }
    }

    if (showRoleDialog && selectedUser != null) {
        RoleEditDialog(
            user = selectedUser!!,
            onDismiss = { showRoleDialog = false },
            onRoleChange = { newRole ->
                viewModel.updateUserRole(selectedUser!!.uid, newRole)
                showRoleDialog = false
            }
        )
    }
}

@Composable
fun RoleEditDialog(
    user: User,
    onDismiss: () -> Unit,
    onRoleChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(user.role) }
    val roles = listOf("admin", "employee")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Role for ${user.name}") },
        text = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                selectedRole = role
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onRoleChange(selectedRole) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
