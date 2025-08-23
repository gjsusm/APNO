package com.example.apno.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apno.admin.categories.CategoryAdminScreen
import com.example.apno.admin.products.ProductAdminScreen
import com.example.apno.admin.products.ProductEditScreen
import com.example.apno.admin.settings.SettingsScreen
import com.example.apno.admin.toppings.ToppingAdminScreen
import com.example.apno.admin.users.UserAdminScreen
import com.example.apno.login.LoginScreen
import com.example.apno.pos.PosScreen
import com.example.apno.reports.ReportsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Pos : Screen("pos")
    object AdminHome : Screen("admin_home")
    object AdminCategories : Screen("admin_categories")
    object AdminProducts : Screen("admin_products")
    object ProductEdit : Screen("product_edit")
    object AdminToppings : Screen("admin_toppings")
    object AdminUsers : Screen("admin_users")
    object AdminSettings : Screen("admin_settings")
    object Reports : Screen("reports")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Pos.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Pos.route) {
            PosScreen(onNavigateToAdmin = { navController.navigate(Screen.AdminHome.route) })
        }
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController = navController)
        }
        composable(Screen.AdminCategories.route) {
            CategoryAdminScreen()
        }
        composable(Screen.AdminProducts.route) {
            ProductAdminScreen(
                onAddProduct = { navController.navigate(Screen.ProductEdit.route) },
                onEditProduct = { productId ->
                    navController.navigate("${Screen.ProductEdit.route}?productId=$productId")
                }
            )
        }
        composable(
            route = "${Screen.ProductEdit.route}?productId={productId}",
            arguments = listOf(navArgument("productId") { nullable = true })
        ) { backStackEntry ->
            ProductEditScreen(productId = backStackEntry.arguments?.getString("productId"))
        }
        composable(Screen.AdminToppings.route) {
            ToppingAdminScreen()
        }
        composable(Screen.AdminUsers.route) {
            UserAdminScreen()
        }
        composable(Screen.AdminSettings.route) {
            SettingsScreen()
        }
        composable(Screen.Reports.route) {
            ReportsScreen()
        }
    }
}

@Composable
fun AdminHomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Admin Home Screen")
            Button(onClick = { navController.navigate(Screen.AdminCategories.route) }) {
                Text("Manage Categories")
            }
            Button(onClick = { navController.navigate(Screen.AdminProducts.route) }) {
                Text("Manage Products")
            }
            Button(onClick = { navController.navigate(Screen.AdminToppings.route) }) {
                Text("Manage Toppings")
            }
            Button(onClick = { navController.navigate(Screen.AdminUsers.route) }) {
                Text("Manage Users")
            }
            Button(onClick = { navController.navigate(Screen.AdminSettings.route) }) {
                Text("Settings")
            }
            Button(onClick = { navController.navigate(Screen.Reports.route) }) {
                Text("Reports")
            }
        }
    }
}
