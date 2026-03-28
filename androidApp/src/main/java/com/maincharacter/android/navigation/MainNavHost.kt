package com.maincharacter.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maincharacter.android.EventDetailScreen
import com.maincharacter.android.EventsScreen
import com.maincharacter.android.GachaScreen
import com.maincharacter.android.HomeScreen
import com.maincharacter.android.InventoryScreen
import com.maincharacter.android.ProfileScreen
import com.maincharacter.android.ShopScreen
import com.maincharacter.android.SignInScreen
import com.maincharacter.android.TaskDetailScreen
import com.maincharacter.android.TasksScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        
        composable(Screen.Tasks.route) {
            TasksScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }
        
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Events.route) {
            EventsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEventDetail = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }
        
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGacha = { navController.navigate(Screen.Gacha.route) },
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onNavigateToShop = { navController.navigate(Screen.Shop.route) }
            )
        }
        
        composable(Screen.Gacha.route) {
            GachaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SignIn.route) {
            SignInScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Shop.route) {
            ShopScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
