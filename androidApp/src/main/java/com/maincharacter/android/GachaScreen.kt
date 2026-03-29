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
        linkedGachaPools.forEach { pool ->
            GachaPoolCard(
                pool = pool,
                ticketCount = currentInventoryTicketCount(appState.inventory),
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
                text = "已接入真实十连：每次消耗 10 张抽卡券，奖励即时写回背包并执行重复奖励转换。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "抽卡券",
                    value = currentInventoryTicketCount(appState.inventory).toString(),
                    accent = Color(0xFFD6C7FF),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "单次十连",
                    value = "10 券",
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "星尘",
                    value = appState.stardustBalance.toString(),
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GachaPoolCard(
    pool: GachaPoolPreview,
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
                        if (canExecute) "可执行" else "券数不足",
                        if (canExecute) Color(0xFF8DFFBB) else Color(0xFFF2B9DA)
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
                        text = if (canExecute) "执行十连" else "抽卡券不足",
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
            Text("本次十连结果", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
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
                        TaskStatusBadge(result.rarity, Color(result.accentArgb))
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
            Text("重复奖励规则", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFE37A))
            Text(
                text = "外观重复转碎片、满级技能重复转道具、道具与碎片重复叠加、星尘直接累加。抽卡完成后结果会立即写回背包。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFF6C4)
            )
        }
    }
}
