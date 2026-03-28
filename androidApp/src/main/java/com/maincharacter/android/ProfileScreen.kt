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
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGacha: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToShop: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileHeroCard()
        ProfileShortcutCard(
            onNavigateToSignIn = onNavigateToSignIn,
            onNavigateToGacha = onNavigateToGacha,
            onNavigateToShop = onNavigateToShop
        )
        ProfileSettingSection()
        ProfileAboutSection()

    }
}
@Composable
internal fun InventoryOverviewCard(
    selectedTab: Int,
    labels: List<String>,
    onTabSelected: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "行囊",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "把技能卡、果实、皮肤和碎片收拢在同一页里，方便从抽卡、事件、任务奖励回流查看。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF20264A)
                ) {
                    Text(
                        text = "商店 / 设置",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB8C4F6)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                labels.forEachIndexed { index, label ->
                    TaskSegmentButton(
                        label = label,
                        selected = selectedTab == index,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    }
}
@Composable
internal fun InventorySkillCard(skill: InventorySkill) {
    Card(
        shape = RoundedCornerShape(24.dp),
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(18.dp),
                        color = skill.accent.copy(alpha = 0.18f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = skill.rarity,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = skill.accent
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EventTagBadge(skill.typeLabel)
                            TaskStatusBadge(skill.stateLabel, skill.accent)
                        }
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = skill.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC8D1FF)
                        )
                    }
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
                    Text(
                        text = "事件联动",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = skill.eventHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4F6)
                    )
                }
            }
        }
    }
}

@Composable
internal fun InventoryItemCard(item: InventoryItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(18.dp),
                color = item.accent.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.shortLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = item.accent
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC8D1FF)
                )
                Text(
                    text = "持有 ${item.count} · ${item.effectHint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = item.accent
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF7588FF)
            ) {
                Text(
                    text = item.actionLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun CompanionWardrobeCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(88.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF20264A)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    SampledResourceImage(
                        resourceId = R.drawable.companion_pose_2,
                        contentDescription = "当前穿戴立绘",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        reqHeightDp = 88.dp
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "当前穿戴 · 星巡礼装",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "外观页重点承接抽卡后的“去换上”，同时把碎片合成和装备反馈做得足够明确。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC8D1FF)
                )
                Text(
                    text = "羁绊加成 +2",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF7B6D1)
                )
            }
        }
    }
}

@Composable
internal fun InventorySkinCard(skin: InventorySkin) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskStatusBadge(skin.rarity, skin.accent)
                        EventTagBadge(if (skin.owned) "已拥有" else "碎片中")
                    }
                    Text(
                        text = skin.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = skin.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (skin.owned) Color(0xFF7588FF) else Color(0xFF20264A)
                ) {
                    Text(
                        text = skin.actionLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = "碎片 ${skin.shardsOwned} / ${skin.shardsRequired}",
                style = MaterialTheme.typography.bodySmall,
                color = skin.accent
            )
        }
    }
}
@Composable
private fun ProfileHeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "我的空间",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "这里承接签到、抽卡、商店之外的账号与设置信息，也可以慢慢长成宿主档案页。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                profileStats.forEach { stat ->
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
private fun ProfileShortcutCard(
    onNavigateToSignIn: () -> Unit,
    onNavigateToGacha: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "高频入口",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            FilledTonalButton(onClick = onNavigateToSignIn, modifier = Modifier.fillMaxWidth()) {
                Text("进入签到")
            }
            FilledTonalButton(onClick = onNavigateToGacha, modifier = Modifier.fillMaxWidth()) {
                Text("进入抽卡")
            }
            FilledTonalButton(onClick = onNavigateToShop, modifier = Modifier.fillMaxWidth()) {
                Text("进入商店")
            }
        }
    }
}

@Composable
private fun ProfileSettingSection() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "设置与权限",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            profileSettings.forEach { setting ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF20264A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = setting.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = setting.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8C4F6)
                            )
                        }
                        Text(
                            text = setting.value,
                            style = MaterialTheme.typography.labelLarge,
                            color = setting.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAboutSection() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5FF))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "关于与版本",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A315D)
            )
            Text(
                text = "主角系统 Demo 目前更偏情绪陪伴和轻养成闭环，后续可以继续接账号同步、隐私政策页和多模态说明。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF41506F)
            )
            profileAboutRows.forEach { row ->
                TaskDetailRow(label = row.first, value = row.second)
            }
        }
    }
}

