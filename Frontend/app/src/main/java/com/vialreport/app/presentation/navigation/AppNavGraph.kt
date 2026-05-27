package com.vialreport.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vialreport.app.data.local.TokenStore
import com.vialreport.app.presentation.admin.AdminStatsScreen
import com.vialreport.app.presentation.auth.login.LoginScreen
import com.vialreport.app.presentation.auth.register.RegisterScreen
import com.vialreport.app.presentation.map.MapScreen
import com.vialreport.app.presentation.report.detail.ReportDetailScreen
import com.vialreport.app.presentation.report.form.ReportFormScreen
import com.vialreport.app.presentation.report.list.ReportListScreen

@Composable
fun AppNavGraph(tokenStore: TokenStore) {
    val navController = rememberNavController()
    val start = if (tokenStore.token != null) Routes.LIST else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess       = { navController.navigate(Routes.LIST) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess  = { navController.navigate(Routes.LIST) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onNavigateToLogin  = { navController.popBackStack() }
            )
        }

        composable(Routes.LIST) { backStackEntry ->
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh_list") == true
            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) backStackEntry.savedStateHandle["refresh_list"] = false
            }
            ReportListScreen(
                onReportClick = { id -> navController.navigate(Routes.detail(id)) },
                onAddClick    = { navController.navigate(Routes.form(null)) },
                onEditClick   = { id -> navController.navigate(Routes.form(id)) },
                onLogout      = {
                    tokenStore.clear()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onMapClick    = { navController.navigate(Routes.MAP) },
                onStatsClick  = { navController.navigate(Routes.STATS) },
                shouldRefresh = shouldRefresh,
                userName      = tokenStore.userName,
                isAdmin       = tokenStore.isAdmin
            )
        }

        composable(
            route     = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            ReportDetailScreen(
                onBack          = { navController.popBackStack() },
                onEdit          = { id -> navController.navigate(Routes.form(id)) },
                onStatusChanged = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_list", true)
                }
            )
        }

        composable(
            route     = Routes.FORM,
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            ReportFormScreen(
                onBack  = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_list", true)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MAP) {
            MapScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STATS) {
            AdminStatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
