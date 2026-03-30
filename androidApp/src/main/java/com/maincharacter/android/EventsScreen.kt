package com.maincharacter.android

import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import com.maincharacter.android.PlaceholderScreen
import com.maincharacter.android.demoEvents
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filteredEvents = when (selectedFilter) {
        1 -> demoEvents.filter { it.unread }
        2 -> demoEvents.filterNot { it.unread }
        else -> demoEvents
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EventOverviewCard(
            selectedFilter = selectedFilter,
            totalCount = demoEvents.size,
            unreadCount = demoEvents.count { it.unread },
            onFilterSelected = { selectedFilter = it }
        )

        filteredEvents.forEach { event ->
            EventListCard(
                event = event,
                onClick = { onNavigateToEventDetail(event.id) }
            )
        }

    }
}
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit = {}
) {
    val event = demoEvents.firstOrNull { it.id == eventId }
    var chapterVideoRes by remember(eventId) { mutableStateOf<Int?>(null) }

    if (event == null) {
        PlaceholderScreen(
            title = "事件详情",
            summary = "没有找到事件 $eventId，后续可以直接接共享层事件配置。",
            primaryLabel = "返回事件列表",
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
        EventDetailHero(event = event)
        EventBranchSection(
            event = event,
            onBranchClick = { branch ->
                AppStateStore.applyEventBranchRewards(branch.rewardChips)
                chapterVideoRes = eventChapterVideoRes(event.id)
            }
        )
        EventResolutionSection(event = event)
        EventCompanionSection(event = event)
        EventComplianceSection()

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回事件列表")
        }
    }

    chapterVideoRes?.let { videoRes ->
        EventChapterVideoDialog(
            videoRes = videoRes,
            chapterTitle = event.title,
            onDismiss = { chapterVideoRes = null }
        )
    }
}
@Composable
private fun EventOverviewCard(
    selectedFilter: Int,
    totalCount: Int,
    unreadCount: Int,
    onFilterSelected: (Int) -> Unit
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
                text = "系统事件",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventFilterChip(
                    label = "全部",
                    selected = selectedFilter == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterSelected(0) }
                )
                EventFilterChip(
                    label = "待处理",
                    selected = selectedFilter == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterSelected(1) }
                )
                EventFilterChip(
                    label = "已经处理",
                    selected = selectedFilter == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterSelected(2) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventSummaryPill(
                    label = "事件总数",
                    value = totalCount.toString(),
                    accent = Color(0xFF90E2FF),
                    modifier = Modifier.weight(1f)
                )
                EventSummaryPill(
                    label = "待处理",
                    value = unreadCount.toString(),
                    accent = Color(0xFFF7B6D1),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
private fun EventFilterChip(
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
private fun EventListCard(
    event: DemoEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EventTagBadge(event.tagLabel)
                        EventStateBadge(event.stateLabel, event.stateColor)
                    }
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = event.intro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC8D1FF)
                    )
                }
                Text(
                    text = event.weightLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD66E),
                    fontWeight = FontWeight.SemiBold
                )
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
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8C4F6)
                    )
                    Text(
                        text = "可选分支 · ${event.branches.size} 个",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                event.branches.forEach { branch ->
                    EventBranchChip(branch = branch)
                }
            }

            Text(
                text = "陪伴收尾 · ${event.companionNote}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF2B9DA)
            )
        }
    }
}

@Composable
private fun EventDetailHero(event: DemoEvent) {
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
                    EventTagBadge(event.tagLabel)
                    EventStateBadge(event.stateLabel, event.stateColor)
                }
                Text(
                    text = event.weightLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD66E),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = event.intro,
                style = MaterialTheme.typography.bodyLarge,
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
                        text = "事件定位",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = event.designNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB8C4F6)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventBranchSection(
    event: DemoEvent,
    onBranchClick: (DemoEventBranch) -> Unit
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
                text = "事件分支",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "系统事件延续 H 类设计方向，保留 2 到 3 个选项、技能联动、低惩罚和短反馈，让章节推进更像养成剧情中的一段互动。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC8D1FF)
            )
            event.branches.forEach { branch ->
                Surface(
                    modifier = Modifier.clickable { onBranchClick(branch) },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF20264A)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = branch.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (branch.requiresSkill != null) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0x2637D5FF)
                                ) {
                                    Text(
                                        text = "需技能 ${branch.requiresSkill}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF90E2FF),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        if (branch.energyCost > 0) {
                            Text(
                                text = "能量消耗 · ${branch.energyCost}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFD66E)
                            )
                        }
                        Text(
                            text = branch.resultPreview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB8C4F6)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            branch.rewardChips.forEach { chip ->
                                RewardChip(chip)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventChapterVideoDialog(
    videoRes: Int,
    chapterTitle: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11162F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = chapterTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "当前章节分支共用同一段剧情视频",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB8C4F6)
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
                ChapterVideoPlayer(
                    videoRes = videoRes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(640.dp)
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("关闭播放器")
                }
            }
        }
    }
}

@Composable
private fun ChapterVideoPlayer(
    videoRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUri = remember(videoRes) {
        Uri.parse("android.resource://${context.packageName}/$videoRes")
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            VideoView(viewContext).apply {
                val controller = MediaController(viewContext).also {
                    it.setAnchorView(this)
                }
                setMediaController(controller)
                setVideoURI(videoUri)
                setOnPreparedListener { mediaPlayer: MediaPlayer ->
                    mediaPlayer.isLooping = true
                    start()
                    controller.show(0)
                }
            }
        },
        update = { videoView ->
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    )
}

private fun eventChapterVideoRes(eventId: String): Int? {
    return when (eventId) {
        "evt_chapter_01_meet" -> R.raw.video01
        "evt_chapter_02_growth" -> R.raw.video02
        "evt_chapter_03_battle" -> R.raw.video03
        "evt_chapter_04_farewell" -> R.raw.video04
        else -> null
    }
}

@Composable
private fun EventResolutionSection(event: DemoEvent) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "触发与结算",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            event.detailRows.forEach { row ->
                TaskDetailRow(label = row.first, value = row.second)
            }
            HorizontalDivider(color = Color(0x332C356A))
            Text(
                text = event.flowSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
        }
    }
}

@Composable
private fun EventCompanionSection(event: DemoEvent) {
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
                text = event.companionNote,
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
                        text = "推荐演出方向",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6565A6)
                    )
                    Text(
                        text = event.performanceHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5B6486)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventComplianceSection() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x221EFFF2))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "表现边界",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9EF7FF)
            )
            Text(
                text = "事件页面统一使用“系统事件 / 虚构剧情”语气，不模仿真实灾害预警、政务通知或现实安全提示。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8F8FF)
            )
        }
    }
}

@Composable
private fun EventStateBadge(
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
private fun EventBranchChip(branch: DemoEventBranch) {
    val bgColor = if (branch.requiresSkill != null) Color(0x1FD6C7FF) else Color(0x1F9ED8FF)
    val contentColor = if (branch.requiresSkill != null) Color(0xFFD6C7FF) else Color(0xFF9ED8FF)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Text(
            text = branch.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

