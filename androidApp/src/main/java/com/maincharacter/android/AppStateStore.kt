package com.maincharacter.android

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

internal data class PersistedTaskState(
    val status: String,
    val progressCurrent: Int
)

internal data class PersistedAppState(
    val bond: Int = demoAccountContext.bond,
    val charm: Int = demoAccountContext.charm,
    val vitality: Int = demoAccountContext.vitality,
    val focus: Int = demoAccountContext.focus,
    val mood: Int = demoAccountContext.mood,
    val lotteryTicketCount: Int = demoAccountContext.lotteryTicketCount,
    val taskStates: Map<String, PersistedTaskState> = defaultTaskStates(),
    val itemCounts: Map<String, Int> = defaultItemCounts(),
    val claimedWeeklyTaskIds: Set<String> = emptySet(),
    val weeklyResetKey: String = currentWeeklyResetKey()
)

internal object AppStateStore {
    private const val PREFS_NAME = "main_character_app_state"
    private const val KEY_STATE = "state"

    private lateinit var appContext: Context
    private lateinit var preferences: SharedPreferences

    private val _state = MutableStateFlow(PersistedAppState())
    val state: StateFlow<PersistedAppState> = _state.asStateFlow()

    val currentState: PersistedAppState
        get() = _state.value

    fun initialize(context: Context) {
        if (::preferences.isInitialized) return
        appContext = context.applicationContext
        preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val loadedState = loadState()
        val normalizedState = refreshWeeklyStateIfNeeded(loadedState)
        _state.value = normalizedState
        if (normalizedState != loadedState) {
            saveState(normalizedState)
        }
    }

    fun completeTask(taskId: String) {
        val baseTask = findTaskBoardTask(taskId) ?: return
        val current = _state.value
        val existing = current.taskStates[taskId]
        if (existing?.status == TaskBoardStatus.COMPLETED.name) return

        var nextState = current.withTask(
            taskId = taskId,
            status = TaskBoardStatus.COMPLETED.name,
            progressCurrent = baseTask.progressTarget
        )

        nextState = when (taskId) {
            "task_daily_home_visit",
            "task_daily_sign_in" -> nextState.copy(bond = (nextState.bond + 2).coerceAtMost(100))

            "task_daily_water_photo" -> nextState.copy(vitality = (nextState.vitality + 2).coerceAtMost(100))

            "task_daily_breakfast_photo" -> nextState.copy(vitality = (nextState.vitality + 3).coerceAtMost(100))

            "task_daily_focus_60s",
            "task_daily_focus_5m" -> nextState.copy(focus = (nextState.focus + 1).coerceAtMost(100))

            "task_today_challenge" -> nextState.copy(
                charm = (nextState.charm + 4).coerceAtMost(100),
                vitality = (nextState.vitality - 5).coerceAtLeast(0)
            )

            else -> nextState
        }

        if (baseTask.sectionKind == TaskBoardSectionKind.DAILY) {
            nextState = updateWeeklyProgress(nextState, baseTask.category)
            nextState = updateChallengeProgress(nextState)
        }

        saveState(nextState)
    }

    fun claimWeeklyTaskReward(taskId: String): Boolean {
        val task = findTaskBoardTask(taskId) ?: return false
        if (task.sectionKind != TaskBoardSectionKind.WEEKLY) return false

        val current = _state.value
        if (taskId in current.claimedWeeklyTaskIds) return false

        val resolvedTask = resolveTaskBoardTask(taskId) ?: return false
        if (!resolvedTask.isFinished) return false

        val rewardCount = when (taskId) {
            "task_weekly_bond" -> 1
            "task_weekly_vitality" -> 2
            "task_weekly_focus" -> 3
            else -> return false
        }

        val nextLotteryCount = current.lotteryTicketCount + rewardCount
        val nextItemCount = (current.itemCounts["lottery_ticket"] ?: 0) + rewardCount
        val nextState = current.copy(
            lotteryTicketCount = nextLotteryCount,
            itemCounts = current.itemCounts + ("lottery_ticket" to nextItemCount),
            claimedWeeklyTaskIds = current.claimedWeeklyTaskIds + taskId
        )
        saveState(nextState)
        return true
    }

    private fun updateWeeklyProgress(
        state: PersistedAppState,
        category: TaskBoardCategory
    ): PersistedAppState {
        val weeklyTaskId = when (category) {
            TaskBoardCategory.BOND -> "task_weekly_bond"
            TaskBoardCategory.VITALITY -> "task_weekly_vitality"
            TaskBoardCategory.FOCUS -> "task_weekly_focus"
            TaskBoardCategory.CHALLENGE -> return state
        }

        val weeklyTask = findTaskBoardTask(weeklyTaskId) ?: return state
        val currentWeekly = state.taskStates[weeklyTaskId]
        val previousProgress = currentWeekly?.progressCurrent ?: weeklyTask.progressCurrent
        val nextProgress = (previousProgress + 1).coerceAtMost(weeklyTask.progressTarget)
        val nextStatus = when {
            nextProgress >= weeklyTask.progressTarget -> TaskBoardStatus.CLAIMABLE
            nextProgress > 0 -> TaskBoardStatus.IN_PROGRESS
            else -> weeklyTask.status
        }

        return state.withTask(
            taskId = weeklyTaskId,
            status = nextStatus.name,
            progressCurrent = nextProgress
        )
    }

    private fun updateChallengeProgress(state: PersistedAppState): PersistedAppState {
        val challengeTask = findTaskBoardTask("task_today_challenge") ?: return state
        val completedCategories = listOf(
            TaskBoardCategory.BOND,
            TaskBoardCategory.VITALITY,
            TaskBoardCategory.FOCUS
        ).count { category ->
            currentTaskBoardContent(state).dailySections
                .flatMap { it.tasks }
                .any { task -> task.category == category && task.isFinished }
        }
        val nextProgress = completedCategories.coerceAtMost(challengeTask.progressTarget)
        val nextStatus = when {
            nextProgress >= challengeTask.progressTarget -> TaskBoardStatus.COMPLETED
            nextProgress > 0 -> TaskBoardStatus.IN_PROGRESS
            else -> TaskBoardStatus.TODO
        }
        return state.withTask(
            taskId = challengeTask.id,
            status = nextStatus.name,
            progressCurrent = nextProgress
        )
    }

    private fun saveState(state: PersistedAppState) {
        _state.value = state
        if (::preferences.isInitialized) {
            preferences.edit().putString(KEY_STATE, encodeState(state)).apply()
        }
        if (::appContext.isInitialized) {
            ValidationDatabaseSync.syncState(appContext, state)
        }
    }

    private fun loadState(): PersistedAppState {
        val raw = preferences.getString(KEY_STATE, null) ?: return PersistedAppState()
        return runCatching {
            refreshWeeklyStateIfNeeded(decodeState(raw))
        }.getOrElse {
            PersistedAppState()
        }
    }

    private fun encodeState(state: PersistedAppState): String {
        val tasksJson = JSONObject().apply {
            state.taskStates.forEach { (taskId, taskState) ->
                put(
                    taskId,
                    JSONObject().apply {
                        put("status", taskState.status)
                        put("progressCurrent", taskState.progressCurrent)
                    }
                )
            }
        }
        val itemsJson = JSONObject().apply {
            state.itemCounts.forEach { (itemId, count) ->
                put(itemId, count)
            }
        }
        return JSONObject().apply {
            put("bond", state.bond)
            put("charm", state.charm)
            put("vitality", state.vitality)
            put("focus", state.focus)
            put("mood", state.mood)
            put("lotteryTicketCount", state.lotteryTicketCount)
            put("tasks", tasksJson)
            put("items", itemsJson)
            put("claimedWeeklyTaskIds", state.claimedWeeklyTaskIds.toList())
            put("weeklyResetKey", state.weeklyResetKey)
        }.toString()
    }

    private fun decodeState(raw: String): PersistedAppState {
        val json = JSONObject(raw)
        val tasksJson = json.optJSONObject("tasks")
        val itemsJson = json.optJSONObject("items")
        val claimedWeeklyTaskIds = buildSet {
            val claimedArray = json.optJSONArray("claimedWeeklyTaskIds") ?: return@buildSet
            for (index in 0 until claimedArray.length()) {
                add(claimedArray.optString(index))
            }
        }
        val taskStates = buildMap {
            tasksJson?.keys()?.forEach { taskId ->
                val taskObject = tasksJson.optJSONObject(taskId) ?: return@forEach
                put(
                    taskId,
                    PersistedTaskState(
                        status = taskObject.optString("status", TaskBoardStatus.TODO.name),
                        progressCurrent = taskObject.optInt("progressCurrent", 0)
                    )
                )
            }
        }
        val itemCounts = buildMap {
            defaultItemCounts().forEach { (itemId, count) ->
                put(itemId, itemsJson?.optInt(itemId, count) ?: count)
            }
        }
        return PersistedAppState(
            bond = json.optInt("bond", demoAccountContext.bond),
            charm = json.optInt("charm", demoAccountContext.charm),
            vitality = json.optInt("vitality", demoAccountContext.vitality),
            focus = json.optInt("focus", demoAccountContext.focus),
            mood = json.optInt("mood", demoAccountContext.mood),
            lotteryTicketCount = json.optInt("lotteryTicketCount", demoAccountContext.lotteryTicketCount),
            taskStates = if (taskStates.isEmpty()) defaultTaskStates() else taskStates,
            itemCounts = itemCounts,
            claimedWeeklyTaskIds = claimedWeeklyTaskIds,
            weeklyResetKey = json.optString("weeklyResetKey", currentWeeklyResetKey())
        )
    }

    private fun refreshWeeklyStateIfNeeded(state: PersistedAppState): PersistedAppState {
        val currentKey = currentWeeklyResetKey()
        if (state.weeklyResetKey == currentKey) return state

        val resetWeeklyTaskIds = taskBoardContent.weeklyTasks.map { it.id }.toSet()
        val resetTaskStates = state.taskStates.toMutableMap().apply {
            taskBoardContent.weeklyTasks.forEach { task ->
                this[task.id] = PersistedTaskState(
                    status = task.status.name,
                    progressCurrent = task.progressCurrent
                )
            }
        }

        return state.copy(
            taskStates = resetTaskStates,
            claimedWeeklyTaskIds = state.claimedWeeklyTaskIds - resetWeeklyTaskIds,
            weeklyResetKey = currentKey
        )
    }

    private fun PersistedAppState.withTask(
        taskId: String,
        status: String,
        progressCurrent: Int
    ): PersistedAppState {
        return copy(
            taskStates = taskStates + (taskId to PersistedTaskState(status, progressCurrent))
        )
    }
}

private fun currentWeeklyResetKey(calendar: Calendar = Calendar.getInstance(Locale.getDefault())): String {
    val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
    val weekYear = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        calendar.weekYear
    } else {
        val cloned = calendar.clone() as Calendar
        val month = cloned.get(Calendar.MONTH)
        when {
            month == Calendar.JANUARY && weekOfYear >= 52 -> cloned.get(Calendar.YEAR) - 1
            month == Calendar.DECEMBER && weekOfYear == 1 -> cloned.get(Calendar.YEAR) + 1
            else -> cloned.get(Calendar.YEAR)
        }
    }
    return "$weekYear-$weekOfYear"
}

internal fun currentHomeStatusMetrics(
    state: PersistedAppState = AppStateStore.currentState
): List<StatusMetric> {
    return listOf(
        StatusMetric("亲密", state.bond.toString(), R.drawable.ic_relation, Color(0xFFF6B7D2)),
        StatusMetric("魅力", "${state.charm}%", R.drawable.ic_home, Color(0xFFAED3FF)),
        StatusMetric("元气", state.vitality.toString(), R.drawable.ic_energy, Color(0xFFC8FF9B)),
        StatusMetric("专注", state.focus.toString(), R.drawable.ic_focus, Color(0xFFD6C7FF))
    )
}

internal fun currentInventoryItems(
    state: PersistedAppState = AppStateStore.currentState
): List<InventoryItem> {
    return inventoryItems.mapIndexed { index, item ->
        val itemId = when (index) {
            0 -> "item_energy_potion"
            1 -> "item_lucky_note"
            2 -> "lottery_ticket"
            else -> item.name
        }
        item.copy(
            count = (state.itemCounts[itemId] ?: item.count.toIntOrNull() ?: 0).toString()
        )
    }
}

private fun defaultTaskStates(): Map<String, PersistedTaskState> {
    val allTasks = taskBoardContent.dailySections.flatMap { it.tasks } +
        taskBoardContent.weeklyTasks +
        taskBoardContent.challengeTask
    return allTasks.associate { task ->
        task.id to PersistedTaskState(
            status = task.status.name,
            progressCurrent = task.progressCurrent
        )
    }
}

private fun defaultItemCounts(): Map<String, Int> {
    return mapOf(
        "item_energy_potion" to 3,
        "item_lucky_note" to 2,
        "lottery_ticket" to demoAccountContext.lotteryTicketCount
    )
}
