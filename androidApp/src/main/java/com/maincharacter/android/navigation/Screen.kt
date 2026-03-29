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
    object PhotoUpload : Screen("photo_upload/{taskId}") {
        fun createRoute(taskId: String) = "photo_upload/$taskId"
    }
    object PhotoCapture : Screen("photo_capture/{taskId}") {
        fun createRoute(taskId: String) = "photo_capture/$taskId"
    }
    object FocusRecord : Screen("focus_record/{taskId}") {
        fun createRoute(taskId: String) = "focus_record/$taskId"
    }
    object VideoUpload : Screen("video_upload/{taskId}") {
        fun createRoute(taskId: String) = "video_upload/$taskId"
    }
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Gacha : Screen("gacha")
    object SignIn : Screen("signin")
    object Shop : Screen("shop")
}
