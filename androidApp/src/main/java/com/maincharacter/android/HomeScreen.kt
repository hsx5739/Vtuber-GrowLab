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
fun HomeScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val metrics = listOf(
        StatusMetric("亲密", "32", R.drawable.ic_relation, Color(0xFFF6B7D2)),
        StatusMetric("魅力", "70%", R.drawable.ic_home, Color(0xFFAED3FF)),
        StatusMetric("元气", "68", R.drawable.ic_energy, Color(0xFFC8FF9B)),
        StatusMetric("专注", "52", R.drawable.ic_focus, Color(0xFFD6C7FF))
    )

    val scenes = listOf(
        CharacterScene(
            imageRes = R.drawable.companion_pose_1,
            mood = "平静",
            message = "今天也一起往前走一点吧。我会在这里看着你完成计划。"
        ),
        CharacterScene(
            imageRes = R.drawable.companion_pose_2,
            mood = "温柔",
            message = "如果累了也没关系，我们可以先从一个很小的目标开始。"
        ),
        CharacterScene(
            imageRes = R.drawable.companion_pose_3,
            mood = "低落",
            message = "我好像有点走神了，要不要陪我做一件确定能完成的小事？"
        ),
        CharacterScene(
            imageRes = R.drawable.companion_pose_4,
            mood = "期待",
            message = "点到我了。今天想先刷任务、看事件，还是只想陪我聊一会儿？"
        )
    )

    var sceneIndex by remember { mutableIntStateOf(0) }
    val currentScene = scenes[sceneIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CharacterStageCard(
            metrics = metrics,
            scene = currentScene,
            onCharacterClick = { sceneIndex = (sceneIndex + 1) % scenes.size }
        )
    }
}

@Composable
private fun CharacterStageCard(
    metrics: List<StatusMetric>,
    scene: CharacterScene,
    onCharacterClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(760.dp)
        ) {
            SampledResourceImage(
                resourceId = R.drawable.home_stage_bg,
                contentDescription = "首页背景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                reqHeightDp = 760.dp,
                useRgb565 = true
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x55273574),
                                Color(0x331C234B),
                                Color(0xCC13172F)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    metrics.chunked(2).forEach { rowMetrics ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMetrics.forEach { metric ->
                                Box(modifier = Modifier.weight(1f)) {
                                    StatusMetricChip(metric = metric)
                                }
                            }
                            if (rowMetrics.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SampledResourceImage(
                        resourceId = scene.imageRes,
                        contentDescription = "主角立绘",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onCharacterClick),
                        contentScale = ContentScale.Fit,
                        reqHeightDp = 560.dp
                    )
                }

                DialoguePanel(
                    mood = scene.mood,
                    message = scene.message
                )
            }
        }
    }
}

@Composable
private fun StatusMetricChip(metric: StatusMetric) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0x66FFFFFF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(12.dp),
                color = metric.accent.copy(alpha = 0.9f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    SampledResourceImage(
                        resourceId = metric.iconRes,
                        contentDescription = metric.label,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                        reqHeightDp = 20.dp,
                        useRgb565 = true
                    )
                }
            }

            Column {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DialoguePanel(
    mood: String,
    message: String
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF7FFF8FF))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "状态  ·  $mood",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF6565A6)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF243B53)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFEAE7F8)
                ) {
                    Text(
                        text = "今天想和我说些什么？",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A7F9A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF7588FF)
                ) {
                    Text(
                        text = "发送",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

