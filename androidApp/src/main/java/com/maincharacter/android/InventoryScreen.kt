package com.maincharacter.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val INFINITE_SYMBOL = "∞"

@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit = {}
) {
    val appState by AppStateStore.state.collectAsState()
    val inventory = appState.inventory
    val skills = remember(inventory) { currentInventorySkills(inventory) }
    val items = remember(inventory) { currentInventoryItems(inventory) }
    val skins = remember(inventory) { currentInventorySkins(inventory) }
    val equippedSkin = remember(inventory) { currentEquippedInventorySkin(inventory) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabLabels = listOf("技能", "道具", "外观", "状态")

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

        InventoryCharacterContextCard(appState)
        InventoryResourceSummaryCard(appState)

        when (selectedTab) {
            0 -> skills.forEach { skill -> InventorySkillCard(skill = skill) }
            1 -> items.forEach { item -> InventoryItemCard(item = item) }
            2 -> {
                CompanionWardrobeCard(skin = equippedSkin)
                skins.forEach { skin ->
                    InventorySkinCard(
                        skin = skin,
                        onAction = {
                            if (skin.owned) {
                                AppStateStore.equipInventorySkin(skin.id)
                            }
                        }
                    )
                }
            }
            else -> {
                CurrencyCard("星尘", INFINITE_SYMBOL, "商城统一消耗货币，商城/抽卡规则共用这一资产口径。", Color(0xFFFFD66E))
                CurrencyCard("抽卡券", currentInventoryTicketCount(inventory).toString(), "用于后续抽卡和活动兑换，数量仍按真实库存展示。", Color(0xFFD6C7FF))
                StatusCard("Bond", appState.bond, Color(0xFFF6B7D2))
                StatusCard("Charm", appState.charm, Color(0xFFAED3FF))
                StatusCard("Vitality", appState.vitality, Color(0xFFC8FF9B))
                StatusCard("Focus", appState.focus, Color(0xFFD6C7FF))
                StatusCard("Mood", appState.mood, Color(0xFF90E2FF))
            }
        }
    }
}

@Composable
private fun InventoryResourceSummaryCard(appState: PersistedAppState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "资源概览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "这里汇总当前背包中的主要资源，方便快速查看库存变化。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "星尘",
                    value = INFINITE_SYMBOL,
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "碎片",
                    value = appState.inventory.shards.values.sumOf { it.count }.toString(),
                    accent = Color(0xFF9ED8FF),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "已解锁外观",
                    value = appState.inventory.skins.values.count { it.isUnlocked }.toString(),
                    accent = Color(0xFFF7B6D1),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CurrencyCard(
    title: String,
    amount: String,
    description: String,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                TaskStatusBadge(amount, accent)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC8D1FF))
        }
    }
}

@Composable
private fun StatusCard(
    label: String,
    value: Int,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("角色当前状态数值。", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
            }
            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.18f)) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(value.toString(), color = accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
