package com.maincharacter.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val appState by AppStateStore.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileHeroCard(appState = appState)
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
                        text = "背包",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "这里统一展示技能、道具、外观，以及任务和签到产出的抽奖券。",
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
internal fun InventoryCharacterContextCard(
    appState: PersistedAppState = AppStateStore.currentState
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${demoAccountContext.nickname} 的人物背包",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "当前账号已绑定这个人物，任务奖励、签到奖励和抽奖券都会写入这里。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "角色 ID",
                    value = demoAccountContext.characterId.removePrefix("char_"),
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "抽奖券",
                    value = currentInventoryTicketCount(appState.inventory).toString(),
                    accent = Color(0xFFD6C7FF),
                    modifier = Modifier.weight(1f)
                )
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
                    text = "持有 ${item.count} - ${item.effectHint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = item.accent
                )
            }
        }
    }
}

@Composable
internal fun CompanionWardrobeCard(skin: InventorySkin) {
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
                        resourceId = skin.imageRes,
                        contentDescription = "当前穿戴立绘",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        reqHeightDp = 88.dp
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "当前穿戴 - ${skin.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "外观页承接换装、碎片合成和穿戴反馈。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC8D1FF)
                )
            }
        }
    }
}

@Composable
internal fun InventorySkinCard(
    skin: InventorySkin,
    onAction: () -> Unit = {}
) {
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
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF20264A)
                ) {
                    SampledResourceImage(
                        resourceId = skin.imageRes,
                        contentDescription = skin.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        reqHeightDp = 92.dp
                    )
                }
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
                    modifier = Modifier.clickable(enabled = skin.owned && skin.actionLabel != "已穿戴") {
                        onAction()
                    },
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
            if (!skin.owned) {
                Text(
                    text = "碎片 ${skin.shardsOwned} / ${skin.shardsRequired}",
                    style = MaterialTheme.typography.bodySmall,
                    color = skin.accent
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    appState: PersistedAppState
) {
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
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF20264A)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${demoAccountContext.nickname} - ${demoAccountContext.accountName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "默认外观：${demoAccountContext.equippedAppearanceName} - mood ${demoAccountContext.mood}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4F6)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentProfileStats(appState).forEach { stat ->
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

private fun currentProfileStats(
    appState: PersistedAppState
): List<ProfileStat> {
    return listOf(
        ProfileStat("角色 ID", demoAccountContext.characterId.removePrefix("char_"), Color(0xFFFFD66E)),
        ProfileStat("魅力值", appState.charm.toString(), Color(0xFFAED3FF)),
        ProfileStat("抽奖券", appState.lotteryTicketCount.toString(), Color(0xFFD6C7FF))
    )
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
            currentProfileSettings().forEach { setting ->
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
                text = "这里展示当前账号、角色和外观信息。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF41506F)
            )
            currentProfileAboutRows().forEach { row ->
                TaskDetailRow(label = row.first, value = row.second)
            }
        }
    }
}

private fun currentProfileSettings(): List<ProfileSettingRow> {
    return listOf(
        ProfileSettingRow(
            title = "健康数据",
            description = "步数、睡眠等验证任务会从这里进入授权与说明。",
            value = "未开启",
            accent = Color(0xFFFFD66E)
        ),
        ProfileSettingRow(
            title = "多模态隐私",
            description = "拍照、语音任务的用途、保留策略和删除说明。",
            value = "查看说明",
            accent = Color(0xFF90E2FF)
        ),
        ProfileSettingRow(
            title = "陪伴音效",
            description = "控制点击反馈、签到提示和待机语音的整体体验。",
            value = "柔和",
            accent = Color(0xFFC8FF9B)
        )
    )
}

private fun currentProfileAboutRows(): List<Pair<String, String>> {
    return listOf(
        "账号" to demoAccountContext.accountName,
        "账号 ID" to demoAccountContext.accountId,
        "角色昵称" to demoAccountContext.nickname,
        "角色外观" to demoAccountContext.equippedAppearanceName
    )
}
