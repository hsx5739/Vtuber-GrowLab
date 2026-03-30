package com.maincharacter.android

import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
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

private data class HomeStatusMetric(
    val label: String,
    val value: String,
    val iconRes: Int,
    val accent: Color
)

private data class HomeScene(
    val imageRes: Int,
    val mood: String,
    val message: String
)

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val appState by AppStateStore.state.collectAsState()
    val conversationController = remember(context) {
        HomeConversationController(context)
    }

    DisposableEffect(conversationController) {
        onDispose { conversationController.dispose() }
    }

    val metrics = remember(appState) {
        listOf(
            HomeStatusMetric("亲密", appState.bond.toString(), R.drawable.ic_relation, Color(0xFFF6B7D2)),
            HomeStatusMetric("魅力", "${appState.charm}%", R.drawable.ic_home, Color(0xFFAED3FF)),
            HomeStatusMetric("元气", appState.vitality.toString(), R.drawable.ic_energy, Color(0xFFC8FF9B)),
            HomeStatusMetric("专注", appState.focus.toString(), R.drawable.ic_focus, Color(0xFFD6C7FF))
        )
    }

    val scenes = remember {
        listOf(
            HomeScene(
                imageRes = R.drawable.companion_pose_1,
                mood = "活力在线",
                message = "今天的状态不错，先把主线任务推进一点，我会一直陪着你。"
            ),
            HomeScene(
                imageRes = R.drawable.companion_pose_2,
                mood = "温柔注视",
                message = "别着急，先把眼前这一步做好，节奏稳下来，效率会更高。"
            ),
            HomeScene(
                imageRes = R.drawable.companion_pose_3,
                mood = "认真督促",
                message = "如果你现在开始执行，我就帮你记住进度，做完之后回来找我汇报。"
            ),
            HomeScene(
                imageRes = R.drawable.companion_pose_4,
                mood = "安静陪伴",
                message = "累了就先休息一下，整理好状态再继续，我希望你今天也能顺利。"
            )
        )
    }

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
            onCharacterClick = {
                val nextIndex = (sceneIndex + 1) % scenes.size
                sceneIndex = nextIndex
                val speakError = conversationController.speakSceneMessage(
                    message = scenes[nextIndex].message,
                    sceneIndex = nextIndex
                )
                if (speakError != null) {
                    Toast.makeText(context, speakError, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun CharacterStageCard(
    metrics: List<HomeStatusMetric>,
    scene: HomeScene,
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0x52FFFFFF)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "小主角 · test_user_01",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "欢迎回到主角系统。点击人物可以切换立绘和下方台词，并自动朗读当前文本。",
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
private fun StatusMetricChip(metric: HomeStatusMetric) {
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
                text = "当前状态 · $mood",
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
                            text = "输入你想对角色说的话",
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
                text = if (isLoading) "正在生成回复" else "角色回应",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6565A6)
            )
            Text(
                text = if (isLoading) "稍等一下，我正在整理要对你说的话。" else replyText.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF243B53)
            )
        }
    }
}
