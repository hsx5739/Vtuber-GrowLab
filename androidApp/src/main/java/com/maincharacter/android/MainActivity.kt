package com.maincharacter.android

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maincharacter.android.navigation.MainNavHost
import com.maincharacter.android.navigation.Screen
import com.maincharacter.android.ui.theme.MainCharacterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainCharacterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0C1026)
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
private fun MainApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val topLevelTabs = listOf(
        TabItem("首页", Screen.Home.route, R.drawable.tab_main),
        TabItem("任务", Screen.Tasks.route, R.drawable.tab_task),
        TabItem("事件", Screen.Events.route, R.drawable.tab_events),
        TabItem("背包", Screen.Inventory.route, R.drawable.tab_inventory),
        TabItem("我的", Screen.Profile.route, R.drawable.tab_profile)
    )

    val showBottomBar = topLevelTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        containerColor = Color(0xFF0C1026),
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0xE0161B36)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            Box(
                                modifier = Modifier
                                    .width(68.dp)
                                    .padding(horizontal = 4.dp)
                            ) {
                                TabButton(
                                    item = tab,
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun TabButton(
    item: TabItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color(0x33FFFFFF) else Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Color(0x26FFFFFF) else Color.Transparent)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                SampledTabIcon(
                    resourceId = item.iconRes,
                    contentDescription = item.label,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = item.label,
                modifier = Modifier.height(18.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else Color(0xFFB5BCE0)
            )
        }
    }
}

@Composable
private fun SampledTabIcon(
    resourceId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(resourceId) {
        decodeSmallBitmap(
            resources = context.resources,
            resourceId = resourceId,
            reqWidth = 72,
            reqHeight = 72
        )?.asImageBitmap()
    }

    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        androidx.compose.foundation.Image(
            painter = painterResource(id = resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

private fun decodeSmallBitmap(
    resources: Resources,
    resourceId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(resources, resourceId, bounds)

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateSmallSampleSize(bounds, reqWidth, reqHeight)
        inPreferredConfig = Bitmap.Config.RGB_565
        inDither = true
        inJustDecodeBounds = false
    }

    return BitmapFactory.decodeResource(resources, resourceId, options)
}

private fun calculateSmallSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize.coerceAtLeast(1)
}

private data class TabItem(
    val label: String,
    val route: String,
    val iconRes: Int
)
