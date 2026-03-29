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
import com.maincharacter.android.PhotoCaptureScreen
import com.maincharacter.android.PhotoUploadScreen
import com.maincharacter.android.ProfileScreen
import com.maincharacter.android.ShopScreen
import com.maincharacter.android.SignInScreen
import com.maincharacter.android.FocusRecordScreen
import com.maincharacter.android.TaskDetailScreen
import com.maincharacter.android.VideoUploadScreen
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onNavigateToPhotoUpload = { targetTaskId ->
                    navController.navigate(Screen.PhotoUpload.createRoute(targetTaskId))
                },
                onNavigateToPhotoCapture = { targetTaskId ->
                    navController.navigate(Screen.PhotoCapture.createRoute(targetTaskId))
                },
                onNavigateToFocusRecord = { targetTaskId ->
                    navController.navigate(Screen.FocusRecord.createRoute(targetTaskId))
                },
                onNavigateToVideoUpload = { targetTaskId ->
                    navController.navigate(Screen.VideoUpload.createRoute(targetTaskId))
                }
            )
        }

        composable(
            route = Screen.PhotoUpload.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            PhotoUploadScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Tasks.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.PhotoCapture.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            PhotoCaptureScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Tasks.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.FocusRecord.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            FocusRecordScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Tasks.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.VideoUpload.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            VideoUploadScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Tasks.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
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
