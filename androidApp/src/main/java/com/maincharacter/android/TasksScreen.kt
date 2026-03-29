package com.maincharacter.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast

@Composable
fun TasksScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val appState by AppStateStore.state.collectAsState()
    var selectedTab by remember { mutableStateOf(TaskBoardTab.DAILY) }
    val board = remember(appState) { currentTaskBoardContent(appState) }
    val overview = if (selectedTab == TaskBoardTab.DAILY) board.dailyOverview else board.weeklyOverview
    val handleTaskClick: (TaskBoardTask) -> Unit = { task ->
        if (task.sectionKind == TaskBoardSectionKind.WEEKLY) {
            onNavigateToTaskDetail(task.id)
        } else if (task.isFinished) {
            Toast.makeText(context, "今日${task.title}已经完成", Toast.LENGTH_SHORT).show()
        } else {
            onNavigateToTaskDetail(task.id)
        }
    }

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
            overview = overview,
            onTabSelected = { selectedTab = it }
        )

        if (selectedTab == TaskBoardTab.DAILY) {
            board.dailySections.forEach { section ->
                TaskSectionTitle(
                    title = section.title,
                    summary = section.summary,
                    accent = section.accent,
                    suffix = "${section.completedCount}/${section.tasks.size}"
                )
                section.tasks.forEach { task ->
                    TaskListCard(
                        task = task,
                        onClick = { handleTaskClick(task) }
                    )
                }
            }

            TaskSectionTitle(
                title = board.challengeTask.category.label,
                summary = board.challengeTask.category.summary,
                accent = board.challengeTask.category.accent,
                suffix = board.challengeTask.progressLabel
            )
            ChallengeHighlightCard(
                task = board.challengeTask,
                onClick = { handleTaskClick(board.challengeTask) }
            )
        } else {
            WeeklyRewardSummaryCard(overview = board.weeklyOverview)
            board.weeklyTasks.forEach { task ->
                TaskListCard(
                    task = task,
                    onClick = { handleTaskClick(task) }
                )
            }
        }
    }
}

@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToPhotoUpload: (String) -> Unit = {},
    onNavigateToPhotoCapture: (String) -> Unit = {},
    onNavigateToFocusRecord: (String) -> Unit = {},
    onNavigateToVideoUpload: (String) -> Unit = {}
) {
    val task = resolveTaskBoardTask(taskId)

    if (task == null) {
        PlaceholderScreen(
            title = "任务详情",
            summary = "没有找到任务 $taskId。",
            primaryLabel = "返回任务列表",
            onPrimaryClick = onNavigateBack
        )
        return
    }

    var actionFeedback by remember(taskId) { mutableStateOf<String?>(null) }

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
        if (task.riskNotes.isNotEmpty()) {
            TaskRiskSection(notes = task.riskNotes)
        }
        TaskCompanionSection(task = task)
        TaskPrimaryActionCard(
            task = task,
            feedback = actionFeedback,
            onPrimaryClick = {
                actionFeedback = when (task.primaryActionLabel) {
                    "去首页" -> {
                        markTaskCompleted(taskId)
                        onNavigateToHome()
                        "已完成该亲密任务，并跳转到首页。"
                    }
                    "去签到" -> {
                        markTaskCompleted(taskId)
                        onNavigateToSignIn()
                        "已完成该亲密任务，并跳转到签到页。"
                    }
                    "上传拍照" -> {
                        onNavigateToPhotoUpload(taskId)
                        "已跳到上传拍照页；选择照片后应进入预览确认，再写入 confirmed 记录。"
                    }
                    "去录像" -> {
                        onNavigateToFocusRecord(taskId)
                        "已跳到录制页；会在点击按钮时动态判断相机和录音权限。"
                    }
                    "上传视频" -> {
                        onNavigateToVideoUpload(taskId)
                        "已跳到视频上传页；选择视频并上传成功后才应结算 charm 奖励。"
                    }
                    "领取抽奖券" -> "点击后应把当前周奖励档位写入角色背包，抽奖券数量立即增加。"
                    else -> "点击后应跳回每日任务继续推进累计次数。"
                }
            },
            onSecondaryClick = {
                actionFeedback = when (task.secondaryActionLabel) {
                    "去拍照" -> {
                        onNavigateToPhotoCapture(taskId)
                        "已进入拍照页；会先判断相机权限，拍完后应进入预览页，再由用户决定“完成”或“重新拍摄”。"
                    }
                    else -> "次级入口已触发。"
                }
            }
        )
    }
}

@Composable
private fun TaskOverviewCard(
    selectedTab: TaskBoardTab,
    overview: TaskBoardOverview,
    onTabSelected: (TaskBoardTab) -> Unit
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
                text = overview.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = overview.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TaskSegmentButton(
                    label = "每日任务",
                    selected = selectedTab == TaskBoardTab.DAILY,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(TaskBoardTab.DAILY) }
                )
                TaskSegmentButton(
                    label = "每周任务",
                    selected = selectedTab == TaskBoardTab.WEEKLY,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(TaskBoardTab.WEEKLY) }
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
                            text = if (selectedTab == TaskBoardTab.DAILY) "今日完成进度" else "本周完成进度",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFB7C5FF)
                        )
                        Text(
                            text = "${overview.completedCount} / ${overview.totalCount}",
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
                                .fillMaxWidth(overview.progress.coerceIn(0f, 1f))
                                .height(10.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF73C6FF), Color(0xFFF6B7D2))
                                    ),
                                    RoundedCornerShape(999.dp)
                                )
                        )
                    }
                    Text(
                        text = overview.rewardHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC8D1FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyRewardSummaryCard(
    overview: TaskBoardOverview
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
                text = "抽奖券分档",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            overview.tiers.forEach { tier ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (tier.reached) Color(0xFF202A4E) else Color(0xFF1A1F3C)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tier.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = tier.progressHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8C4F6)
                            )
                        }
                        TaskStatusBadge(
                            label = "${tier.ticketTotal} 张",
                            color = if (tier.reached) Color(0xFF8DFFBB) else Color(0xFFFFD66E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSectionTitle(
    title: String,
    summary: String,
    accent: Color,
    suffix: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB8C4F6)
            )
        }
        TaskStatusBadge(label = suffix, color = accent)
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
private fun ChallengeHighlightCard(
    task: TaskBoardTask,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2247))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x332A396E), Color(0x111B2247))
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskStatusBadge(label = "高难度", color = Color(0xFFFFD66E))
                    TaskStatusBadge(label = task.status.label, color = task.statusColor)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    task.rewardChips.forEach { RewardChip(it) }
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
            }
        }
    }
}

@Composable
private fun TaskListCard(
    task: TaskBoardTask,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isFinished) Color(0xFF12172D) else Color(0xFF161B37)
        )
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
                        TaskTypeBadge(task.category.label)
                        TaskTypeBadge(
                            when (task.sectionKind) {
                                TaskBoardSectionKind.DAILY -> "每日"
                                TaskBoardSectionKind.WEEKLY -> "每周"
                                TaskBoardSectionKind.CHALLENGE -> "挑战"
                            }
                        )
                        TaskStatusBadge(task.status.label, task.statusColor)
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
                task.rewardChips.forEach { RewardChip(it) }
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
private fun TaskDetailHero(task: TaskBoardTask) {
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
                    TaskTypeBadge(task.category.label)
                    TaskTypeBadge(
                        when (task.sectionKind) {
                            TaskBoardSectionKind.DAILY -> "每日任务"
                            TaskBoardSectionKind.WEEKLY -> "每周任务"
                            TaskBoardSectionKind.CHALLENGE -> "今日挑战"
                        }
                    )
                    TaskStatusBadge(task.status.label, task.statusColor)
                }
                Text(
                    text = task.progressLabel,
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
private fun TaskRewardSection(task: TaskBoardTask) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "奖励与判定",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = task.primaryActionHint,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                task.rewardChips.forEach { RewardChip(it) }
            }
            HorizontalDivider(color = Color(0x332C356A))
            task.detailRows.forEach { row ->
                TaskDetailRow(label = row.first, value = row.second)
            }
        }
    }
}

@Composable
private fun TaskGuideSection(task: TaskBoardTask) {
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
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF7588FF)
                        ) {
                            Text(
                                text = "${index + 1}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
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
private fun TaskRiskSection(notes: List<String>) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "风险与限制",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            notes.forEach { note ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF20264A)
                ) {
                    Text(
                        text = note,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCompanionSection(task: TaskBoardTask) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5FF))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "陪伴反馈",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A315D)
            )
            Text(
                text = task.companionLine,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF41506F)
            )
        }
    }
}

@Composable
private fun TaskPrimaryActionCard(
    task: TaskBoardTask,
    feedback: String?,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: (() -> Unit)? = null
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
                text = "主按钮",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "详情页固定只保留 1 个主按钮，文案随任务类型和挑战阶段变化。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC8D1FF)
            )
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(task.primaryActionLabel)
            }
            if (task.secondaryActionLabel != null) {
                FilledTonalButton(
                    onClick = { onSecondaryClick?.invoke() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(task.secondaryActionLabel)
                }
                if (task.secondaryActionHint != null) {
                    Text(
                        text = task.secondaryActionHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC8D1FF)
                    )
                }
            }
            if (feedback != null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF20264A)
                ) {
                    Text(
                        text = feedback,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4F6)
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
