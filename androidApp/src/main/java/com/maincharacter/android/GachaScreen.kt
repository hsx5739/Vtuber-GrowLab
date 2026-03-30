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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maincharacter.shared.model.Inventory

private const val INFINITE_SYMBOL = "∞"

private data class LocalGachaRewardPreview(
    val rewardType: String,
    val targetName: String,
    val amountText: String,
    val rarity: String,
    val weight: Int,
    val duplicateRule: String
)

private data class LocalGachaPoolPreview(
    val poolId: String,
    val name: String,
    val description: String,
    val ticketCost: Int,
    val featuredNote: String,
    val rewards: List<LocalGachaRewardPreview>
)

private val localGachaPools = listOf(
    LocalGachaPoolPreview(
        poolId = "pool_shared_assets_v1",
        name = "共享资源池",
        description = "消耗抽卡券即可执行抽取，结果会立即写回背包。",
        ticketCost = 10,
        featuredNote = "当前资源池覆盖技能、道具、碎片、抽卡券与星尘。",
        rewards = listOf(
            LocalGachaRewardPreview("技能", "护盾术", "解锁/重复转化", "SR", 20, "重复转化"),
            LocalGachaRewardPreview("道具", "元气药剂", "+1", "R", 35, "可叠加"),
            LocalGachaRewardPreview("碎片", "夜航校服", "+1", "SR", 18, "可叠加"),
            LocalGachaRewardPreview("抽卡券", "抽卡券", "+1", "R", 12, "直接入包"),
            LocalGachaRewardPreview("星尘", "STAR_DUST", "5~100", "N", 15, "直接累加")
        )
    )
)

@Composable
fun GachaScreen(
    onNavigateBack: () -> Unit = {}
) {
    val appState by AppStateStore.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GachaOverviewCard(appState)
        localGachaPools.forEach { pool ->
            GachaPoolCard(
                pool = pool,
                ticketCount = localInventoryTicketCount(appState.inventory),
                onExecute = { AppStateStore.executeGacha(pool.poolId) }
            )
        }
        if (appState.latestGachaResults.isNotEmpty()) {
            GachaResultCard(appState.latestGachaResults)
        }
        GachaRuleCard()
    }
}

@Composable
private fun GachaOverviewCard(appState: PersistedAppState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "抽卡",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "抽卡会消耗抽卡券，奖励会立即写回背包。星尘在这里以无限符号展示。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "抽卡券",
                    value = localInventoryTicketCount(appState.inventory).toString(),
                    accent = Color(0xFFD6C7FF),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "单次消耗",
                    value = "10 券",
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "星尘",
                    value = INFINITE_SYMBOL,
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GachaPoolCard(
    pool: LocalGachaPoolPreview,
    ticketCount: Int,
    onExecute: () -> Unit
) {
    val canExecute = ticketCount >= pool.ticketCost

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatusBadge(
                        label = if (canExecute) "可抽取" else "券不足",
                        color = if (canExecute) Color(0xFF8DFFBB) else Color(0xFFF2B9DA)
                    )
                    Text(pool.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(pool.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC8D1FF))
                }
                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF20264A)) {
                    Text(
                        text = "${pool.ticketCost} 抽卡券",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFFFD66E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF20264A)) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("奖池说明", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Text(pool.featuredNote, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
                }
            }

            pool.rewards.forEach { reward ->
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF20264A)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${reward.rewardType} · ${reward.targetName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "数量 ${reward.amountText} | 稀有度 ${reward.rarity} | 重复规则 ${reward.duplicateRule}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8C4F6)
                            )
                        }
                        Text("权重 ${reward.weight}", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFD66E))
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canExecute) { onExecute() },
                shape = RoundedCornerShape(18.dp),
                color = if (canExecute) Color(0xFF7588FF) else Color(0xFF2A315D)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (canExecute) "执行抽卡" else "抽卡券不足",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GachaResultCard(results: List<GachaPullResult>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("抽卡结果", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            results.forEach { result ->
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF20264A)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(result.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(result.detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
                        }
                        TaskStatusBadge(label = result.rarity, color = Color(result.accentArgb))
                    }
                }
            }
        }
    }
}

@Composable
private fun GachaRuleCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFF1A8))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("规则说明", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFE37A))
            Text(
                text = "重复外观会转碎片，重复技能会转资源，道具和星尘奖励会直接累加到背包。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFF6C4)
            )
        }
    }
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
