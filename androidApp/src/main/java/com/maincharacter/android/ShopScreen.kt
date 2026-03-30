package com.maincharacter.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maincharacter.shared.model.Inventory
import kotlinx.coroutines.launch

private const val INFINITE_SYMBOL = "∞"

private enum class LocalShopRewardType {
    ITEM,
    SKIN_SHARD,
    TICKET,
    STATUS,
    STARDUST
}

private data class LocalShopProductDefinition(
    val productId: String,
    val sectionId: String,
    val name: String,
    val description: String,
    val price: Int,
    val rewardType: LocalShopRewardType,
    val rewardTargetId: String? = null,
    val rewardAmount: Int,
    val accent: Color,
    val badge: String? = null
)

private data class LocalShopSectionDefinition(
    val sectionId: String,
    val title: String,
    val summary: String
)

private val localShopSections = listOf(
    LocalShopSectionDefinition(
        sectionId = "supply",
        title = "补给",
        summary = "用于补充道具和状态资源。"
    ),
    LocalShopSectionDefinition(
        sectionId = "wardrobe",
        title = "外观碎片",
        summary = "用于解锁和推进外观收集。"
    ),
    LocalShopSectionDefinition(
        sectionId = "gacha",
        title = "抽卡券",
        summary = "将星尘兑换为后续抽卡所需的抽卡券。"
    )
)

private val localShopProducts = listOf(
    LocalShopProductDefinition(
        productId = "shop_supply_energy_potion",
        sectionId = "supply",
        name = "元气药剂",
        description = "恢复日常行动所需的基础状态资源。",
        price = 80,
        rewardType = LocalShopRewardType.ITEM,
        rewardTargetId = InventoryCatalog.itemEnergyPotion,
        rewardAmount = 1,
        accent = Color(0xFFC8FF9B),
        badge = "道具"
    ),
    LocalShopProductDefinition(
        productId = "shop_supply_lucky_note",
        sectionId = "supply",
        name = "幸运便签",
        description = "提高日常互动中的正反馈感受。",
        price = 120,
        rewardType = LocalShopRewardType.ITEM,
        rewardTargetId = InventoryCatalog.itemLuckyNote,
        rewardAmount = 1,
        accent = Color(0xFFFFE37A),
        badge = "道具"
    ),
    LocalShopProductDefinition(
        productId = "shop_status_bond_pack",
        sectionId = "supply",
        name = "亲密礼包",
        description = "购买后立即提升 bond 状态。",
        price = 90,
        rewardType = LocalShopRewardType.STATUS,
        rewardTargetId = "BOND",
        rewardAmount = 1,
        accent = Color(0xFFF6B7D2),
        badge = "状态"
    ),
    LocalShopProductDefinition(
        productId = "shop_skin_night_school_shard",
        sectionId = "wardrobe",
        name = "夜航校服碎片",
        description = "用于解锁夜航校服外观。",
        price = 600,
        rewardType = LocalShopRewardType.SKIN_SHARD,
        rewardTargetId = InventoryCatalog.skinNightSchool,
        rewardAmount = 1,
        accent = Color(0xFFFFD66E),
        badge = "碎片"
    ),
    LocalShopProductDefinition(
        productId = "shop_skin_morning_casual_shard",
        sectionId = "wardrobe",
        name = "晨雾便服碎片",
        description = "用于解锁晨雾便服外观。",
        price = 400,
        rewardType = LocalShopRewardType.SKIN_SHARD,
        rewardTargetId = InventoryCatalog.skinMorningCasual,
        rewardAmount = 1,
        accent = Color(0xFF9ED8FF),
        badge = "碎片"
    ),
    LocalShopProductDefinition(
        productId = "shop_lottery_ticket",
        sectionId = "gacha",
        name = "抽卡券",
        description = "抽卡使用的基础消耗券。",
        price = 50,
        rewardType = LocalShopRewardType.TICKET,
        rewardTargetId = InventoryCatalog.itemLotteryTicket,
        rewardAmount = 1,
        accent = Color(0xFFD6C7FF),
        badge = "兑换"
    )
)

@Composable
fun ShopScreen(
    onNavigateBack: () -> Unit = {}
) {
    val appState by AppStateStore.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
    ) {
        SnackbarHost(hostState = snackbarHostState)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ShopOverviewCard(appState)
            localShopSections.forEach { section ->
                val products = localShopProducts.filter { it.sectionId == section.sectionId }
                ShopSectionCard(
                    title = section.title,
                    summary = section.summary,
                    products = products,
                    appState = appState,
                    onPurchase = { product ->
                        val result = AppStateStore.purchaseShopProduct(product.productId)
                        scope.launch {
                            snackbarHostState.showSnackbar(result.message)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShopOverviewCard(appState: PersistedAppState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "商城资源",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "商城中的星尘仅按无限符号展示，商品价格与购买结果保持真实逻辑。",
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
                    label = "抽卡券",
                    value = localInventoryTicketCount(appState.inventory).toString(),
                    accent = Color(0xFFD6C7FF),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "碎片库存",
                    value = appState.inventory.shards.values.sumOf { it.count }.toString(),
                    accent = Color(0xFF9ED8FF),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShopSectionCard(
    title: String,
    summary: String,
    products: List<LocalShopProductDefinition>,
    appState: PersistedAppState,
    onPurchase: (LocalShopProductDefinition) -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC8D1FF))
            products.forEach { product ->
                ShopProductCard(
                    product = product,
                    appState = appState,
                    onPurchase = onPurchase
                )
            }
        }
    }
}

@Composable
private fun ShopProductCard(
    product: LocalShopProductDefinition,
    appState: PersistedAppState,
    onPurchase: (LocalShopProductDefinition) -> Unit
) {
    val afford = appState.stardustBalance >= product.price
    val statusLabel = when {
        !afford -> "星尘不足"
        product.rewardType == LocalShopRewardType.SKIN_SHARD && isSkinUnlocked(appState, product.rewardTargetId) -> "已解锁"
        else -> "可购买"
    }
    val statusColor = when {
        !afford -> Color(0xFFF2B9DA)
        statusLabel == "已解锁" -> Color(0xFF90E2FF)
        else -> product.accent
    }

    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF20264A)) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        product.badge?.let { EventTagBadge(it) }
                        TaskStatusBadge(statusLabel, statusColor)
                    }
                    Text(product.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(product.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
                }
                Text("${product.price} 星尘", style = MaterialTheme.typography.labelLarge, color = product.accent, fontWeight = FontWeight.SemiBold)
            }

            Text(
                text = buildRewardPreview(product),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE3E7FF)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = afford) { onPurchase(product) },
                shape = RoundedCornerShape(16.dp),
                color = if (afford) Color(0xFF7588FF) else Color(0xFF2A315D)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (afford) "立即购买" else "资源不足",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun buildRewardPreview(product: LocalShopProductDefinition): String {
    return when (product.rewardType) {
        LocalShopRewardType.ITEM -> "奖励：道具 +${product.rewardAmount}"
        LocalShopRewardType.SKIN_SHARD -> "奖励：外观碎片 +${product.rewardAmount}"
        LocalShopRewardType.TICKET -> "奖励：抽卡券 +${product.rewardAmount}"
        LocalShopRewardType.STATUS -> "奖励：状态 ${product.rewardTargetId} +${product.rewardAmount}"
        LocalShopRewardType.STARDUST -> "奖励：星尘 +${product.rewardAmount}"
    }
}

private fun isSkinUnlocked(appState: PersistedAppState, skinId: String?): Boolean {
    if (skinId == null) return false
    return appState.inventory.skins[skinId]?.isUnlocked == true
}

@Composable
private fun EventSummaryPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF21274A)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color(0xFFB8C4F6))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun EventTagBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0x26FFFFFF)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
private fun TaskStatusBadge(
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.18f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun localInventoryTicketCount(inventory: Inventory): Int {
    return inventory.items[InventoryCatalog.itemLotteryTicket]?.count ?: 0
}
