package com.vialreport.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vialreport.app.presentation.report.detail.ReportDetailScreen
import com.vialreport.app.presentation.report.form.ReportFormScreen
import com.vialreport.app.presentation.report.list.ReportListScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(Routes.LIST) { backStackEntry ->

            val shouldRefresh =
                backStackEntry.savedStateHandle.get<Boolean>("refresh_list") == true

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    backStackEntry.savedStateHandle["refresh_list"] = false
                }
            }

            ReportListScreen(
                onReportClick = { id ->
                    navController.navigate(Routes.detail(id))
                },
                onAddClick = {
                    navController.navigate(Routes.form(null))
                },
                onEditClick = { id ->
                    navController.navigate(Routes.form(id))
                },
                shouldRefresh = shouldRefresh
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) {
            ReportDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.form(id)) }
            )
        }

        composable(
            route = Routes.FORM,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ReportFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_list", true)
                    navController.popBackStack()
                }
            )
        }
    }
}
