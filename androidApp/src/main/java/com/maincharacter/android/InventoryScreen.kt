package com.maincharacter.android

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabLabels = listOf("技能", "道具", "外观")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InventoryOverviewCard(
            selectedTab = selectedTab,
            labels = tabLabels,
            onTabSelected = { selectedTab = it }
        )

        InventoryCharacterContextCard()

        when (selectedTab) {
            0 -> {
                inventorySkills.forEach { skill ->
                    InventorySkillCard(skill = skill)
                }
            }

            1 -> {
                inventoryItems.forEach { item ->
                    InventoryItemCard(item = item)
                }
            }

            else -> {
                CompanionWardrobeCard()
                inventorySkins.forEach { skin ->
                    InventorySkinCard(skin = skin)
                }
            }
        }

    }
}
