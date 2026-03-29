package com.maincharacter.android

import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch

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
            linkedShopSections.forEach { section ->
                val products = linkedShopProducts.filter { it.sectionId == section.sectionId }
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
                text = "商城联动",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "全部商品已按真实奖励定义接入：统一消耗星尘，购买结果即时写入背包或角色状态。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "星尘",
                    value = appState.stardustBalance.toString(),
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "抽卡券",
                    value = currentInventoryTicketCount(appState.inventory).toString(),
                    accent = Color(0xFFD6C7FF),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "碎片总量",
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
    products: List<ShopProductDefinition>,
    appState: PersistedAppState,
    onPurchase: (ShopProductDefinition) -> Unit
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
    product: ShopProductDefinition,
    appState: PersistedAppState,
    onPurchase: (ShopProductDefinition) -> Unit
) {
    val afford = appState.stardustBalance >= product.price
    val statusLabel = when {
        !afford -> "星尘不足"
        product.rewardType == ShopRewardType.SKIN_SHARD && isSkinUnlocked(appState, product.rewardTargetId) -> "已拥有外观"
        else -> "可购买"
    }
    val statusColor = when {
        !afford -> Color(0xFFF2B9DA)
        statusLabel == "已拥有外观" -> Color(0xFF90E2FF)
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
                        text = if (afford) "立即购买" else "余额不足",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun buildRewardPreview(product: ShopProductDefinition): String {
    return when (product.rewardType) {
        ShopRewardType.ITEM -> "奖励：背包道具 +${product.rewardAmount}"
        ShopRewardType.SKIN_SHARD -> "奖励：对应外观碎片 +${product.rewardAmount}"
        ShopRewardType.TICKET -> "奖励：抽卡券 +${product.rewardAmount}"
        ShopRewardType.STATUS -> "奖励：角色状态 ${product.rewardTargetId} +${product.rewardAmount}"
        ShopRewardType.STARDUST -> "奖励：星尘 +${product.rewardAmount}"
    }
}

private fun isSkinUnlocked(appState: PersistedAppState, skinId: String?): Boolean {
    if (skinId == null) return false
    return appState.inventory.skins[skinId]?.isUnlocked == true
}
