package com.maincharacter.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val conversationController = remember(context) {
        HomeConversationController(context)
    }

    DisposableEffect(conversationController) {
        onDispose { conversationController.dispose() }
    }

    val metrics = homeStatusMetrics

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
            message = "轮到我啦。今天想先刷任务、看事件，还是只想陪我聊一会儿？"
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
            replyText = conversationController.replyText,
            isLoading = conversationController.isLoading,
            inputText = conversationController.inputText,
            onInputChange = conversationController::onInputChange,
            onSendClick = conversationController::sendCurrentMessage,
            onCharacterClick = { sceneIndex = (sceneIndex + 1) % scenes.size }
        )
    }
}

@Composable
private fun CharacterStageCard(
    metrics: List<StatusMetric>,
    scene: CharacterScene,
    replyText: String?,
    isLoading: Boolean,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
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
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0x52FFFFFF)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${demoAccountContext.nickname} · ${demoAccountContext.accountName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "角色绑定已生效，任务奖励和背包资产统一写入当前人物上下文",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD8DEFF)
                            )
                        }
                    }
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

                    if (replyText != null || isLoading) {
                        CompanionReplyBubble(
                            replyText = replyText,
                            isLoading = isLoading,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 24.dp, end = 4.dp)
                                .widthIn(max = 230.dp)
                        )
                    }
                }

                DialoguePanel(
                    mood = scene.mood,
                    message = scene.message,
                    inputText = inputText,
                    isLoading = isLoading,
                    onInputChange = onInputChange,
                    onSendClick = onSendClick
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
    message: String,
    inputText: String,
    isLoading: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
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
                text = "状态 · $mood",
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
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    minLines = 2,
                    maxLines = 4,
                    placeholder = {
                        Text(
                            text = "今天想和我说些什么？",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEAE7F8),
                        unfocusedContainerColor = Color(0xFFEAE7F8),
                        focusedBorderColor = Color(0xFF7588FF),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF243B53),
                        unfocusedTextColor = Color(0xFF243B53)
                    )
                )

                Surface(
                    modifier = Modifier.clickable(onClick = onSendClick),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF7588FF),
                    border = if (isLoading) BorderStroke(1.dp, Color(0x80FFFFFF)) else null
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "发送",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionReplyBubble(
    replyText: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color(0xF2FFF8FF),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isLoading) "正在回复" else "陪伴回复",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6565A6)
            )
            Text(
                text = if (isLoading) "我在想一想，马上就告诉你。" else replyText.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF243B53)
            )
        }
    }
}
