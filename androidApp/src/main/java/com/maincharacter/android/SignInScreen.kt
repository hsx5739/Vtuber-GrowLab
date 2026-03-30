package com.maincharacter.android

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maincharacter.shared.model.SignInState
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

@Composable
fun SignInScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val appState by AppStateStore.state.collectAsState()
    val todayCalendar = Calendar.getInstance()
    val todayKey = formatDateKey(todayCalendar)
    val signInState = appState.signInState
    val weekEntries = buildSignInWeekEntries(signInState, todayCalendar)
    val milestones = buildMilestones(signInState.streak)
    val signedToday = signInState.lastSignInDate == todayKey

    LaunchedEffect(todayKey) {
        AppStateStore.refreshForToday(todayKey)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1026))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SignInSummaryCard(
            monthTitle = "${todayCalendar.get(Calendar.MONTH) + 1}月",
            signedToday = signedToday,
            streak = signInState.streak,
            totalSignIns = signInState.totalSignIns
        )
        SignInWeekCard(weekEntries = weekEntries)
        SignInFeedbackCard(weekEntries = weekEntries)
        Button(
            onClick = {
                val result = AppStateStore.signInToday(todayKey)
                val message = if (result.alreadySigned) {
                    "今日已签到"
                } else if (result.rewardedTickets > 0) {
                    "今日已签到，连签奖励已入背包"
                } else {
                    "今日已签到"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (!result.alreadySigned) {
                    onNavigateBack()
                }
            },
            enabled = !signedToday,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (signedToday) "今日已签到" else "签到")
        }
        SignInMilestoneCard(
            milestones = milestones,
            lotteryTicketCount = appState.lotteryTicketCount
        )
    }
}

@Composable
private fun SignInSummaryCard(
    monthTitle: String,
    signedToday: Boolean,
    streak: Int,
    totalSignIns: Int
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
                text = monthTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (signedToday) "今天已经来过了，节奏继续保持。" else "今天也来打个照面。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8D1FF)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SignInStatPill(
                    label = "今日状态",
                    value = if (signedToday) "已签到" else "待签到",
                    accent = if (signedToday) Color(0xFF8DFFBB) else Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                SignInStatPill(
                    label = "连续签到",
                    value = "$streak 天",
                    accent = Color(0xFFFFD66E),
                    modifier = Modifier.weight(1f)
                )
                SignInStatPill(
                    label = "累计签到",
                    value = "$totalSignIns 天",
                    accent = Color(0xFF90E2FF),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SignInWeekCard(
    weekEntries: List<SignInWeekEntry>
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B37))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "本周签到簿",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekEntries.forEach { entry ->
                    SignInDayCell(
                        entry = entry,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SignInFeedbackCard(
    weekEntries: List<SignInWeekEntry>
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C39))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "这周的轻提醒",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            weekEntries.forEach { entry ->
                val containerColor = when {
                    entry.isToday && entry.isSigned -> Color(0xFF25345B)
                    entry.isToday -> Color(0xFF1E2B52)
                    entry.isSigned -> Color(0xFF1A2344)
                    else -> Color(0xFF131933)
                }
                val contentColor = when {
                    entry.isToday || entry.isSigned -> Color.White
                    else -> Color(0xFF9CA8D8)
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = containerColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskStatusBadge(
                            label = entry.weekLabel,
                            color = if (entry.isToday) Color(0xFFFFD66E) else Color(0xFF90E2FF)
                        )
                        Text(
                            text = entry.feedback,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                        if (entry.isSigned) {
                            Text(
                                text = "已签",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF8DFFBB),
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
private fun SignInMilestoneCard(
    milestones: List<SignInMilestoneUi>,
    lotteryTicketCount: Int
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A213D))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "连续签到奖励",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "当前背包抽奖券 $lotteryTicketCount 张",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB8C4F6)
            )
            milestones.forEach { milestone ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF20284A)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "1 张抽奖券",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8C4F6)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = milestone.progress,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFFE37A),
                                fontWeight = FontWeight.SemiBold
                            )
                            TaskStatusBadge(
                                label = milestone.statusLabel,
                                color = milestone.statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInDayCell(
    entry: SignInWeekEntry,
    modifier: Modifier = Modifier
) {
    val containerBrush = when {
        entry.isToday && entry.isSigned -> Brush.verticalGradient(
            listOf(Color(0xFF7A8CFF), Color(0xFF5A6BD6))
        )
        entry.isToday -> Brush.verticalGradient(
            listOf(Color(0xFF2E3F76), Color(0xFF202B52))
        )
        entry.isSigned -> Brush.verticalGradient(
            listOf(Color(0xFF24305C), Color(0xFF1A2242))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF1B2242), Color(0xFF151B34))
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(containerBrush, RoundedCornerShape(18.dp))
                .padding(vertical = 12.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.weekLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (entry.isToday) Color(0xFFFFF1A8) else Color(0xFFB8C4F6)
                )
                Text(
                    text = entry.dayLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = when {
                        entry.isToday && entry.isSigned -> "已签"
                        entry.isToday -> "今天"
                        entry.isSigned -> "完成"
                        else -> "未签"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.isSigned) Color(0xFF8DFFBB) else Color(0xFF9CA8D8)
                )
            }
        }
    }
}

@Composable
private fun SignInStatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF21274A)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB8C4F6)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun TaskStatusBadge(
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

private data class SignInWeekEntry(
    val weekLabel: String,
    val dayLabel: String,
    val isToday: Boolean,
    val isSigned: Boolean,
    val feedback: String
)

private data class SignInMilestoneUi(
    val title: String,
    val progress: String,
    val statusLabel: String,
    val statusColor: Color
)

private fun buildSignInWeekEntries(
    signInState: SignInState,
    today: Calendar
): List<SignInWeekEntry> {
    val weekLabels = DateFormatSymbols(Locale.CHINA).shortWeekdays
    val weekStart = (today.clone() as Calendar).apply {
        while (get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            add(Calendar.DAY_OF_MONTH, -1)
        }
    }
    return signInFeedbackTexts.mapIndexed { index, feedback ->
        val date = (weekStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, index)
        }
        SignInWeekEntry(
            weekLabel = weekLabels[date.get(Calendar.DAY_OF_WEEK)],
            dayLabel = date.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0'),
            isToday = isSameCalendarDay(date, today),
            isSigned = signInState.isSignedInMonth(formatDateKey(date)),
            feedback = feedback
        )
    }
}

private fun buildMilestones(streak: Int): List<SignInMilestoneUi> {
    val targets = listOf(3, 7, 15)
    val activeTarget = targets.firstOrNull { streak < it }
    return targets.map { target ->
        val status = when {
            streak >= target -> Triple("已达成", Color(0xFF8DFFBB), "$target/$target")
            activeTarget == target -> Triple("进行中", Color(0xFFFFD66E), "${min(streak, target)}/$target")
            else -> Triple("未达成", Color(0xFF9CA8D8), "${min(streak, target)}/$target")
        }
        SignInMilestoneUi(
            title = "连续 $target 天",
            progress = status.third,
            statusLabel = status.first,
            statusColor = status.second
        )
    }
}

private fun formatDateKey(calendar: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}

private fun isSameCalendarDay(left: Calendar, right: Calendar): Boolean {
    return left.get(Calendar.YEAR) == right.get(Calendar.YEAR) &&
        left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR)
}

private val signInFeedbackTexts = listOf(
    "今天也来打个照面。",
    "保持出现，本身就很难得。",
    "不用很满，记得来过就好。",
    "小小签到，也算在认真生活。",
    "这一天，被你轻轻点亮了。",
    "留下一次出现，节奏就没断。",
    "今天也和自己站在一起。"
)
