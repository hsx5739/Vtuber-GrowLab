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
fun TasksScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val visibleTasks = if (selectedTab == 0) demoTasks.filter { it.group == "DAILY" } else demoTasks.filter { it.group == "WEEKLY" }
    val completedCount = visibleTasks.count { it.status == "已完成" || it.status == "待领取加成" }
    val progress = if (visibleTasks.isNotEmpty()) completedCount.toFloat() / visibleTasks.size else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TaskOverviewCard(
            selectedTab = selectedTab,
            progress = progress,
            completedCount = completedCount,
            totalCount = visibleTasks.size,
            onTabSelected = { selectedTab = it }
        )

        visibleTasks.forEach { task ->
            TaskListCard(
                task = task,
                onClick = { onNavigateToTaskDetail(task.id) }
            )
        }

        FilledTonalButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回上一页")
        }
    }
}

@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {}
) {
    val task = demoTasks.firstOrNull { it.id == taskId }

    if (task == null) {
        PlaceholderScreen(
            title = "任务详情",
            summary = "没有找到任务 $taskId，对应的数据后续可以改成读取共享层配置。",
            primaryLabel = "返回任务列表",
            onPrimaryClick = onNavigateBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TaskDetailHero(task = task)
        TaskRewardSection(task = task)
        TaskGuideSection(task = task)
        TaskCompanionSection(task = task)

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回任务列表")
        }
    }
}

@Composable
private fun TaskOverviewCard(
    selectedTab: Int,
    progress: Float,
    completedCount: Int,
    totalCount: Int,
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
            Text(
                text = "今日委托",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (selectedTab == 0) {
                    "点完成永远有基础反馈，但更高价值奖励会绑定计时、步数、拍照、语音这类验证层。"
                } else {
                    "本周奖励鼓励多样性完成，不靠单纯连点自述任务来堆资源。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TaskSegmentButton(
                    label = "今日",
                    selected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(0) }
                )
                TaskSegmentButton(
                    label = "本周",
                    selected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(1) }
                )
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF21274A)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "完成进度",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFB7C5FF)
                        )
                        Text(
                            text = "$completedCount / $totalCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color(0xFF2A315D), RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(10.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF73C6FF), Color(0xFF9C8CFF))
                                    ),
                                    RoundedCornerShape(999.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TaskSegmentButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFF7588FF) else Color(0xFF242A4A)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TaskListCard(
    task: DemoTask,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskTypeBadge(task.type)
                        TaskStatusBadge(task.status, task.statusColor)
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
                Text(
                    text = task.progressLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF90E2FF),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                task.rewardChips.forEach { chip ->
                    RewardChip(chip)
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
                        text = task.validationTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = task.validationHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4F6)
                    )
                }
            }

            Text(
                text = "陪伴提示 · ${task.companionLine}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF2B9DA)
            )
        }
    }
}

@Composable
private fun TaskDetailHero(task: DemoTask) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskTypeBadge(task.type)
                    TaskStatusBadge(task.status, task.statusColor)
                }
                Text(
                    text = task.groupLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF9EACE8)
                )
            }
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF20264A)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "任务定位",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = task.designNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB8C4F6)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRewardSection(task: DemoTask) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "奖励与验证",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "文档里要求“点完成有基础反馈，验证通过给硬通货或额外加成”，这里先按展示态做出来。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                task.rewardChips.forEach { chip ->
                    RewardChip(chip)
                }
            }
            HorizontalDivider(color = Color(0x332C356A))
            task.detailRows.forEach { row ->
                TaskDetailRow(label = row.first, value = row.second)
            }
        }
    }
}

@Composable
private fun TaskGuideSection(task: DemoTask) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "完成方式",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            task.actionSteps.forEachIndexed { index, step ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF20264A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF7588FF)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = step,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC8D1FF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCompanionSection(task: DemoTask) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5FF))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "陪伴者反馈",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A315D)
            )
            Text(
                text = task.companionLine,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF41506F)
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEAE7F8)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "推荐操作",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6565A6)
                    )
                    Text(
                        text = task.validationHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5B6486)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskTypeBadge(type: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0x2637D5FF)
    ) {
        Text(
            text = type,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF90E2FF),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun TaskStatusBadge(
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

@Composable
internal fun RewardChip(chip: RewardChipData) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chip.background
    ) {
        Text(
            text = "${chip.label} ${chip.value}",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = chip.contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
@Composable
internal fun TaskDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB8C4F6)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
