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
fun ShopScreen(
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ShopOverviewCard()
        shopSections.forEach { section ->
            ShopSectionCard(section = section)
        }
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回上一页")
        }
    }
}
@Composable
internal fun GachaOverviewCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "祈愿",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "先做 Demo 版单卡池：展示星尘、月华、单抽券和基础保底说明，重点承接皮肤、技能卡、碎片的收集闭环。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                profileWalletStats.forEach { stat ->
                    EventSummaryPill(
                        label = stat.label,
                        value = stat.value,
                        accent = stat.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
@Composable
internal fun GachaPoolCard(pool: DemoPool) {
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
                    TaskStatusBadge(pool.highlight, pool.highlightColor)
                    Text(
                        text = pool.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = pool.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF20264A)
                ) {
                    Text(
                        text = pool.costLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFFFD66E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF20264A)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("掉落重点", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Text(pool.dropHint, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF7588FF)
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text("单抽一次", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF242A4A)
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text("查看记录", color = Color(0xFFB8C4F6), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
internal fun GachaRuleCard() {
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
                text = "当前参考 `pool_normal`：100 星尘单抽，90 抽内至少出稀有。页面上保留概率、公示和未成年人保护入口，不把抽卡做成赌场式刺激。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFF6C4)
            )
        }
    }
}

@Composable
internal fun SignInHeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("今日签到", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = "签到页重点是“半屏日历 + 连签奖励 + 温和留存反馈”。今天先做出完整展示态，后续再接真实日界与幂等逻辑。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                signInSummaryStats.forEach { stat ->
                    EventSummaryPill(
                        label = stat.label,
                        value = stat.value,
                        accent = stat.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SignInCalendarCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("三月签到簿", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                signInDays.chunked(4).forEach { columnDays ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        columnDays.forEach { day ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = if (day.signed) Color(0xFF7588FF) else Color(0xFF20264A)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(day.day, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(day.reward, color = Color(0xFFE3E7FF), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF7588FF)) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("领取今日奖励", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun SignInMilestoneCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5FF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("连续签到奖励", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2A315D))
            signInMilestones.forEach { milestone ->
                TaskDetailRow(label = milestone.first, value = milestone.second)
            }
        }
    }
}
@Composable
private fun ShopOverviewCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("商店", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = "先承接养成物资、皮肤碎片和互动礼物。Demo 里以页面展示和商品结构为主，付费链路后续再接沙箱。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shopWalletStats.forEach { stat ->
                    EventSummaryPill(
                        label = stat.label,
                        value = stat.value,
                        accent = stat.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
@Composable
private fun ShopSectionCard(section: ShopSection) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(section.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(section.summary, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC8D1FF))
            section.items.forEach { item ->
                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF20264A)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4F6))
                        }
                        Text(item.price, style = MaterialTheme.typography.labelLarge, color = item.accent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

