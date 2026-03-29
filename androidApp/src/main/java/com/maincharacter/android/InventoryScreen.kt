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
import com.maincharacter.shared.model.CurrencyType

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
    val tabLabels = listOf("技能", "道具", "外观", "资源")

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
                CurrencyCard("星尘", appState.stardustBalance.toString(), "商城统一消耗货币，商城/抽卡规则共用这一资产口径。", Color(0xFFFFD66E))
                CurrencyCard("抽卡券", currentInventoryTicketCount(inventory).toString(), "已同步到背包与抽卡页，后续抽卡执行层会直接读取该库存。", Color(0xFFD6C7FF))
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
                text = "联动结果",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "商城购买和后续抽卡奖励都会通过同一套库存/状态源刷新到背包，不再依赖页面本地假数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "星尘",
                    value = appState.inventory.currencies[CurrencyType.STAR_DUST]?.toString() ?: "0",
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
                    label = "已拥有外观",
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
                Text("商城补给购买后会立即更新该状态值。", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
            }
            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.18f)) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(value.toString(), color = accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
