package com.maincharacter.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ScreenBackground = Color(0xFF0C1026)
private val PanelBackground = Color(0xFF171C39)
private val PanelBackgroundSecondary = Color(0xFF21274A)
private val AccentBlue = Color(0xFF8DB4FF)
private val AccentPink = Color(0xFFF2A8C6)
private val AccentMint = Color(0xFFA7E7C7)
private val AccentGold = Color(0xFFFFD66E)

private data class DemoTask(
    val id: String,
    val title: String,
    val category: String,
    val progress: String,
    val reward: String,
    val description: String
)

private data class DemoEvent(
    val id: String,
    val title: String,
    val tag: String,
    val summary: String,
    val outcome: String
)

private data class InventoryEntry(
    val name: String,
    val kind: String,
    val description: String
)

private val demoTasks = listOf(
    DemoTask("daily_focus", "Complete 25 minutes of focus", "Daily", "1 / 1", "Mood +10", "Finish one short focus session."),
    DemoTask("daily_walk", "Walk 3000 steps", "Daily", "2300 / 3000", "Energy +8", "Use a small action to restart the day."),
    DemoTask("weekly_reflect", "Write one weekly note", "Weekly", "0 / 1", "Bond +15", "Record one important thing from this week.")
)

private val demoEvents = listOf(
    DemoEvent("evt_evening_breeze", "Evening Breeze", "Companion", "A soft reminder appears on the home screen.", "Gain a small mood response."),
    DemoEvent("evt_lucky_ping", "Lucky Ping", "Bonus", "A light surprise appears after a task.", "Gain a small resource reward."),
    DemoEvent("evt_midnight_note", "Midnight Note", "Night", "A short note appears in the evening scene.", "Gain a short companion response.")
)

private val inventoryEntries = listOf(
    InventoryEntry("Vitality Fruit", "Item", "A short boost for task atmosphere."),
    InventoryEntry("Wish Ticket", "Ticket", "Reserved for gacha entry."),
    InventoryEntry("Barrier", "Skill", "Unlocks an extra option in some events."),
    InventoryEntry("Morning Outfit", "Skin", "A casual companion skin.")
)

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Main Character",
            subtitle = "Home dashboard for tasks, events, inventory and profile."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(Modifier.weight(1f), "Mood", "72", AccentPink)
            MetricCard(Modifier.weight(1f), "Focus", "58", AccentBlue)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(Modifier.weight(1f), "Energy", "68", AccentMint)
            MetricCard(Modifier.weight(1f), "Bond", "Lv.4", AccentGold)
        }

        SectionCard(
            title = "Quick Access",
            subtitle = "Bottom back buttons have been removed from all pages."
        ) {
            ActionButton("Tasks", onNavigateToTasks)
            ActionButton("Events", onNavigateToEvents)
            ActionButton("Inventory", onNavigateToInventory)
            ActionButton("Profile", onNavigateToProfile)
        }
    }
}

@Composable
fun TasksScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Tasks",
            subtitle = "Task list without a bottom back button."
        )

        demoTasks.forEach { task ->
            ClickableInfoCard(
                title = task.title,
                badge = task.category,
                lines = listOf(
                    "Progress: ${task.progress}",
                    "Reward: ${task.reward}",
                    task.description
                ),
                onClick = { onNavigateToTaskDetail(task.id) }
            )
        }
    }
}

@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {}
) {
    val task = demoTasks.firstOrNull { it.id == taskId }

    ScreenScaffold {
        HeroCard(
            title = task?.title ?: "Task Detail",
            subtitle = task?.description ?: "Task not found."
        )

        SectionCard(title = "Task Info") {
            InfoRow("Task ID", taskId)
            InfoRow("Category", task?.category ?: "Unknown")
            InfoRow("Progress", task?.progress ?: "N/A")
            InfoRow("Reward", task?.reward ?: "N/A")
        }

        SectionCard(title = "Note") {
            BodyText("No bottom back button is rendered on this page.")
        }
    }
}

@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Events",
            subtitle = "Event list without a bottom back button."
        )

        demoEvents.forEach { event ->
            ClickableInfoCard(
                title = event.title,
                badge = event.tag,
                lines = listOf(event.summary, event.outcome),
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

    ScreenScaffold {
        HeroCard(
            title = event?.title ?: "Event Detail",
            subtitle = event?.summary ?: "Event not found."
        )

        SectionCard(title = "Event Info") {
            InfoRow("Event ID", eventId)
            InfoRow("Tag", event?.tag ?: "Unknown")
            InfoRow("Outcome", event?.outcome ?: "N/A")
        }

        SectionCard(title = "Note") {
            BodyText("No bottom back button is rendered on this page.")
        }
    }
}

@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Inventory",
            subtitle = "Items, skills and skins overview."
        )

        inventoryEntries.forEach { entry ->
            SectionCard(title = entry.name, subtitle = entry.kind) {
                BodyText(entry.description)
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGacha: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToShop: () -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Profile",
            subtitle = "Profile, resources and secondary entries."
        )

        SectionCard(title = "Status") {
            InfoRow("Sign-in Streak", "7 days")
            InfoRow("Bond Level", "Lv.4")
            InfoRow("Stardust", "380")
        }

        SectionCard(title = "Shortcuts") {
            ActionButton("Sign In", onNavigateToSignIn)
            ActionButton("Gacha", onNavigateToGacha)
            ActionButton("Shop", onNavigateToShop)
        }
    }
}

@Composable
fun GachaScreen(
    onNavigateBack: () -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Gacha",
            subtitle = "Pool overview without a bottom back button."
        )

        SectionCard(title = "Pool") {
            InfoRow("Name", "Standard Wish")
            InfoRow("Single Pull", "100 Stardust")
            InfoRow("Pity", "90 pulls")
        }
    }
}

@Composable
fun SignInScreen(
    onNavigateBack: () -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Sign In",
            subtitle = "Daily sign-in summary."
        )

        SectionCard(title = "Status") {
            InfoRow("Today", "Available")
            InfoRow("Streak", "7 days")
            InfoRow("Milestone", "Ticket x1")
        }
    }
}

@Composable
fun ShopScreen(
    onNavigateBack: () -> Unit = {}
) {
    ScreenScaffold {
        HeroCard(
            title = "Shop",
            subtitle = "Exchange overview without a bottom back button."
        )

        SectionCard(title = "Supply") {
            InfoRow("Vitality Gift", "80 Stardust")
            InfoRow("Companion Gift", "120 Stardust")
        }

        SectionCard(title = "Cosmetics") {
            InfoRow("Morning Outfit Dye", "4 Moonlight")
            InfoRow("School Uniform Shard", "6 Moonlight")
        }
    }
}

@Composable
private fun ScreenScaffold(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PanelBackground)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFFC8D1FF),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PanelBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color(0xFFB6C0F0),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            content()
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = PanelBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(6.dp)
                    .background(accent, RoundedCornerShape(999.dp))
            )
            Text(
                text = label,
                color = Color(0xFFB7C5FF),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ClickableInfoCard(
    title: String,
    badge: String,
    lines: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PanelBackgroundSecondary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0x337588FF)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color(0xFFDCE3FF),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            lines.forEach { line ->
                BodyText(line)
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label)
    }
}

@Composable
private fun InfoRow(
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
            color = Color(0xFFB7C5FF),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BodyText(
    value: String
) {
    Text(
        text = value,
        color = Color(0xFFD8DDF2),
        style = MaterialTheme.typography.bodyMedium
    )
}
