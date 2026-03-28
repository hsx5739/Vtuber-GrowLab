package com.maincharacter.android.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Tasks : Screen("tasks")
    object Events : Screen("events")
    object Inventory : Screen("inventory")
    object Profile : Screen("profile")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Gacha : Screen("gacha")
    object SignIn : Screen("signin")
    object Shop : Screen("shop")
}