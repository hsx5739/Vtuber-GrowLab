package com.maincharacter.android

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.maincharacter.shared.model.CurrencyType
import com.maincharacter.shared.model.Inventory
import com.maincharacter.shared.model.ItemState
import com.maincharacter.shared.model.ShardState
import com.maincharacter.shared.model.SignInState
import com.maincharacter.shared.model.SkinState
import com.maincharacter.shared.model.SkinUnlockSource
import com.maincharacter.shared.serializer.DataSerializer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import java.util.TimeZone
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
    val stardustBalance: Int = TEST_STARDUST_BALANCE,
    val lotteryTicketCount: Int = demoAccountContext.lotteryTicketCount,
    val signInState: SignInState = defaultSignInState(),
    val taskStates: Map<String, PersistedTaskState> = defaultTaskStates(),
    val itemCounts: Map<String, Int> = defaultItemCounts(),
    val inventory: Inventory = defaultInventoryState(),
    val latestGachaResults: List<GachaPullResult> = emptyList(),
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
        val normalizedState = normalizeState(loadedState)
        _state.value = normalizedState
        if (normalizedState != loadedState) {
            saveState(normalizedState)
        }
    }

    fun completeTask(taskId: String) {
        val current = _state.value
        val nextState = completeTaskInternal(current, taskId) ?: return
        saveState(nextState)
    }

    fun signInToday(date: String = currentDateKey()): SignInActionResult {
        val normalizedCurrent = normalizeState(_state.value, date)
        if (normalizedCurrent.signInState.lastSignInDate == date) {
            if (normalizedCurrent != _state.value) {
                saveState(normalizedCurrent)
            }
            return SignInActionResult(
                alreadySigned = true,
                streak = normalizedCurrent.signInState.streak,
                rewardedTickets = 0
            )
        }

        var nextState = normalizedCurrent
        val nextSignInState = updateSignInState(normalizedCurrent.signInState, date)
        val rewardedTickets = signInMilestoneTickets(nextSignInState.streak)

        nextState = nextState.copy(signInState = nextSignInState)
        if (rewardedTickets > 0) {
            nextState = nextState.copy(
                lotteryTicketCount = nextState.lotteryTicketCount + rewardedTickets,
                itemCounts = nextState.itemCounts + (
                    "lottery_ticket" to ((nextState.itemCounts["lottery_ticket"] ?: 0) + rewardedTickets)
                )
            )
        }

        nextState = completeTaskInternal(nextState, "task_daily_sign_in") ?: nextState
        saveState(nextState)

        return SignInActionResult(
            alreadySigned = false,
            streak = nextSignInState.streak,
            rewardedTickets = rewardedTickets
        )
    }

    fun refreshForToday(date: String = currentDateKey()) {
        val normalized = normalizeState(_state.value, date)
        if (normalized != _state.value) {
            saveState(normalized)
        }
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

    fun purchaseShopProduct(productId: String): ShopPurchaseResult {
        val product = linkedShopProducts.firstOrNull { it.productId == productId }
            ?: return ShopPurchaseResult(false, "商品不存在")

        val current = _state.value
        if (current.stardustBalance < product.price) {
            return ShopPurchaseResult(false, "星尘不足")
        }

        var nextState = current.copy(stardustBalance = current.stardustBalance - product.price)
        nextState = when (product.rewardType) {
            ShopRewardType.ITEM -> {
                val itemId = product.rewardTargetId ?: return ShopPurchaseResult(false, "商品配置缺少目标")
                val currentItem = nextState.inventory.items[itemId]
                val nextCount = (currentItem?.count ?: 0) + product.rewardAmount
                val nextItems = nextState.inventory.items + (
                    itemId to (currentItem ?: ItemState(
                        itemId = itemId,
                        count = nextCount,
                        isConsumable = InventoryCatalog.itemDefinitions[itemId]?.isConsumable == true
                    )).copy(count = nextCount)
                )
                nextState.copy(
                    itemCounts = nextState.itemCounts + (itemId to nextCount),
                    inventory = nextState.inventory.copy(items = nextItems, lastUpdateTime = System.currentTimeMillis())
                )
            }

            ShopRewardType.SKIN_SHARD -> {
                val skinId = product.rewardTargetId ?: return ShopPurchaseResult(false, "商品配置缺少目标")
                val currentShard = nextState.inventory.shards[skinId]
                val nextCount = (currentShard?.count ?: 0) + product.rewardAmount
                val nextShards = nextState.inventory.shards + (
                    skinId to (currentShard ?: ShardState(skinId = skinId, count = nextCount)).copy(
                        count = nextCount,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                )
                val currentSkin = nextState.inventory.skins[skinId]
                val nextSkins = if (currentSkin != null) {
                    nextState.inventory.skins + (skinId to currentSkin.copy(shardCount = nextCount))
                } else {
                    nextState.inventory.skins
                }
                nextState.copy(
                    inventory = nextState.inventory.copy(
                        shards = nextShards,
                        skins = nextSkins,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                )
            }

            ShopRewardType.TICKET -> {
                nextState.copy(lotteryTicketCount = nextState.lotteryTicketCount + product.rewardAmount)
            }

            ShopRewardType.STATUS -> {
                val statusTarget = product.rewardTargetId ?: return ShopPurchaseResult(false, "商品配置缺少目标")
                when (StatusType.valueOf(statusTarget)) {
                    StatusType.BOND -> nextState.copy(bond = (nextState.bond + product.rewardAmount).coerceAtMost(100))
                    StatusType.CHARM -> nextState.copy(charm = (nextState.charm + product.rewardAmount).coerceAtMost(100))
                    StatusType.VITALITY -> nextState.copy(vitality = (nextState.vitality + product.rewardAmount).coerceAtMost(100))
                    StatusType.FOCUS -> nextState.copy(focus = (nextState.focus + product.rewardAmount).coerceAtMost(100))
                    StatusType.MOOD -> nextState.copy(mood = (nextState.mood + product.rewardAmount).coerceAtMost(100))
                }
            }

            ShopRewardType.STARDUST -> {
                nextState.copy(stardustBalance = nextState.stardustBalance + product.rewardAmount)
            }
        }

        saveState(normalizeState(nextState))
        return ShopPurchaseResult(success = true, message = buildPurchaseMessage(product))
    }

    fun executeGacha(poolId: String): GachaActionResult {
        val pool = linkedGachaPools.firstOrNull { it.poolId == poolId }
            ?: return GachaActionResult(false, "奖池不存在")
        val current = _state.value
        if (current.lotteryTicketCount < pool.ticketCost) {
            return GachaActionResult(false, "抽卡券不足，至少需要 ${pool.ticketCost} 张")
        }

        var nextState = current.copy(
            lotteryTicketCount = current.lotteryTicketCount - pool.ticketCost,
            latestGachaResults = emptyList()
        )
        val results = mutableListOf<GachaPullResult>()
        repeat(pool.ticketCost) {
            val reward = pickGachaReward(pool)
            val applied = applyGachaReward(nextState, reward)
            nextState = applied.first
            results += applied.second
        }
        nextState = nextState.copy(latestGachaResults = results)
        saveState(normalizeState(nextState))
        return GachaActionResult(true, "十连完成，共获得 ${results.size} 项奖励")
    }

    fun equipInventorySkin(skinId: String): Boolean {
        val current = _state.value
        val skinState = current.inventory.skins[skinId] ?: return false
        if (!skinState.isUnlocked) return false
        if (current.inventory.equippedSkinId == skinId) return false

        val now = System.currentTimeMillis()
        val nextInventory = current.inventory.copy(
            equippedSkinId = skinId,
            skins = current.inventory.skins.mapValues { (id, state) ->
                state.copy(
                    isEquipped = id == skinId,
                    equipTime = if (id == skinId) now else state.equipTime
                )
            },
            lastUpdateTime = now
        )
        saveState(current.copy(inventory = nextInventory))
        return true
    }

    fun applyEventBranchRewards(rewardChips: List<RewardChipData>) {
        if (rewardChips.isEmpty()) return

        var nextState = _state.value
        rewardChips.forEach { chip ->
            val amount = parseRewardAmount(chip.value) ?: return@forEach
            nextState = when (chip.label) {
                "亲密", "羁绊" -> nextState.copy(
                    bond = (nextState.bond + amount).coerceIn(0, 100)
                )
                "魅力", "心境" -> nextState.copy(
                    charm = (nextState.charm + amount).coerceIn(0, 100)
                )
                "元气", "活力" -> nextState.copy(
                    vitality = (nextState.vitality + amount).coerceIn(0, 100)
                )
                "专注" -> nextState.copy(
                    focus = (nextState.focus + amount).coerceIn(0, 100)
                )
                "星尘" -> nextState.copy(
                    stardustBalance = (nextState.stardustBalance + amount).coerceAtLeast(0)
                )
                else -> nextState
            }
        }

        saveState(normalizeState(nextState))
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
            normalizeState(decodeState(raw))
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
            put("stardustBalance", state.stardustBalance)
            put("lotteryTicketCount", state.lotteryTicketCount)
            put(
                "signIn",
                JSONObject().apply {
                    put("lastSignInDate", state.signInState.lastSignInDate)
                    put("streak", state.signInState.streak)
                    put("totalSignIns", state.signInState.totalSignIns)
                    put("monthSignedBits", state.signInState.monthSignedBits)
                    put("currentMonth", state.signInState.currentMonth)
                    put("lastMonthSignedBits", state.signInState.lastMonthSignedBits)
                    put("lastMonth", state.signInState.lastMonth)
                    put("timeZone", state.signInState.timeZone)
                }
            )
            put("tasks", tasksJson)
            put("items", itemsJson)
            put("inventory", DataSerializer.serializeInventory(state.inventory))
            put(
                "latestGachaResults",
                state.latestGachaResults.map { result ->
                    JSONObject().apply {
                        put("title", result.title)
                        put("detail", result.detail)
                        put("rarity", result.rarity)
                        put("accentArgb", result.accentArgb)
                    }
                }
            )
            put("claimedWeeklyTaskIds", state.claimedWeeklyTaskIds.toList())
            put("weeklyResetKey", state.weeklyResetKey)
        }.toString()
    }

    private fun decodeState(raw: String): PersistedAppState {
        val json = JSONObject(raw)
        val tasksJson = json.optJSONObject("tasks")
        val itemsJson = json.optJSONObject("items")
        val signInJson = json.optJSONObject("signIn")
        val inventoryJson = json.optString("inventory", "")
        val latestGachaResults = buildList {
            val array = json.optJSONArray("latestGachaResults") ?: return@buildList
            for (index in 0 until array.length()) {
                val resultObject = array.optJSONObject(index) ?: continue
                add(
                    GachaPullResult(
                        title = resultObject.optString("title"),
                        detail = resultObject.optString("detail"),
                        rarity = resultObject.optString("rarity"),
                        accentArgb = resultObject.optLong("accentArgb").toInt()
                    )
                )
            }
        }
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
            stardustBalance = json.optInt("stardustBalance", TEST_STARDUST_BALANCE),
            lotteryTicketCount = json.optInt("lotteryTicketCount", demoAccountContext.lotteryTicketCount),
            signInState = if (signInJson == null) {
                defaultSignInState()
            } else {
                SignInState(
                    lastSignInDate = signInJson.optString("lastSignInDate", ""),
                    streak = signInJson.optInt("streak", 0),
                    totalSignIns = signInJson.optInt("totalSignIns", 0),
                    monthSignedBits = signInJson.optLong("monthSignedBits", 0L),
                    currentMonth = signInJson.optString("currentMonth", currentMonthKey()),
                    lastMonthSignedBits = signInJson.optLong("lastMonthSignedBits", 0L),
                    lastMonth = signInJson.optString("lastMonth", ""),
                    timeZone = signInJson.optString("timeZone", TimeZone.getDefault().id)
                )
            },
            taskStates = if (taskStates.isEmpty()) defaultTaskStates() else taskStates,
            itemCounts = itemCounts,
            inventory = inventoryJson.takeIf { it.isNotBlank() }?.let {
                runCatching { DataSerializer.deserializeInventory(it) }.getOrElse {
                    defaultInventoryState(json.optInt("lotteryTicketCount", demoAccountContext.lotteryTicketCount))
                }
            } ?: defaultInventoryState(json.optInt("lotteryTicketCount", demoAccountContext.lotteryTicketCount)),
            latestGachaResults = latestGachaResults,
            claimedWeeklyTaskIds = claimedWeeklyTaskIds,
            weeklyResetKey = json.optString("weeklyResetKey", currentWeeklyResetKey())
        )
    }

    private fun completeTaskInternal(
        current: PersistedAppState,
        taskId: String
    ): PersistedAppState? {
        val baseTask = findTaskBoardTask(taskId) ?: return null
        val existing = current.taskStates[taskId]
        if (existing?.status == TaskBoardStatus.COMPLETED.name) return null

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

        return nextState
    }

    private fun normalizeState(
        state: PersistedAppState,
        today: String = currentDateKey()
    ): PersistedAppState {
        var normalized = refreshWeeklyStateIfNeeded(state).copy(stardustBalance = TEST_STARDUST_BALANCE)
        val normalizedSignIn = normalizeSignInState(normalized.signInState, today)
        if (normalizedSignIn != normalized.signInState) {
            normalized = normalized.copy(signInState = normalizedSignIn)
        }
        normalized = syncSignInTaskState(normalized, today)
        return syncInventoryState(normalized)
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

internal data class SignInActionResult(
    val alreadySigned: Boolean,
    val streak: Int,
    val rewardedTickets: Int
)

private fun normalizeSignInState(
    signInState: SignInState,
    today: String = currentDateKey()
): SignInState {
    val currentMonth = currentMonthKey(today)
    val timeZone = TimeZone.getDefault().id
    var normalized = signInState

    if (normalized.currentMonth != currentMonth) {
        val previousMonth = normalized.currentMonth
        val previousBits = normalized.monthSignedBits
        normalized = normalized.copy(
            monthSignedBits = 0L,
            currentMonth = currentMonth,
            lastMonthSignedBits = previousBits,
            lastMonth = previousMonth,
            monthlyMilestones = emptySet(),
            timeZone = timeZone
        )
    } else if (normalized.timeZone != timeZone) {
        normalized = normalized.copy(timeZone = timeZone)
    }

    if (normalized.lastSignInDate.isNotBlank()) {
        val gap = daysBetween(normalized.lastSignInDate, today)
        if (gap > 1) {
            normalized = normalized.copy(streak = 0)
        }
    }

    return normalized
}

private fun syncSignInTaskState(
    state: PersistedAppState,
    today: String = currentDateKey()
): PersistedAppState {
    val signInTask = findTaskBoardTask("task_daily_sign_in") ?: return state
    val isSignedToday = state.signInState.lastSignInDate == today
    val desiredStatus = if (isSignedToday) TaskBoardStatus.COMPLETED.name else signInTask.status.name
    val desiredProgress = if (isSignedToday) signInTask.progressTarget else signInTask.progressCurrent
    val currentTaskState = state.taskStates["task_daily_sign_in"]

    return if (
        currentTaskState?.status == desiredStatus &&
        currentTaskState?.progressCurrent == desiredProgress
    ) {
        state
    } else {
        state.copy(
            taskStates = state.taskStates + (
                "task_daily_sign_in" to PersistedTaskState(
                    status = desiredStatus,
                    progressCurrent = desiredProgress
                )
            )
        )
    }
}

private fun updateSignInState(
    signInState: SignInState,
    date: String
): SignInState {
    val normalized = normalizeSignInState(signInState, date)
    val previousDate = normalized.lastSignInDate.takeIf { it.isNotBlank() }
    val nextStreak = when {
        previousDate == null -> 1
        previousDate == date -> normalized.streak
        daysBetween(previousDate, date) == 1 -> normalized.streak + 1
        else -> 1
    }
    val dayBit = 1L shl (dayOfMonthFromDate(date) - 1)

    return normalized.copy(
        lastSignInDate = date,
        streak = nextStreak,
        totalSignIns = normalized.totalSignIns + 1,
        monthSignedBits = normalized.monthSignedBits or dayBit,
        currentMonth = currentMonthKey(date),
        timeZone = TimeZone.getDefault().id
    )
}

private fun signInMilestoneTickets(streak: Int): Int {
    return if (streak in setOf(3, 7, 15)) 1 else 0
}

private fun defaultSignInState(today: String = currentDateKey()): SignInState {
    return SignInState(
        currentMonth = currentMonthKey(today),
        timeZone = TimeZone.getDefault().id
    )
}

private fun currentMonthKey(today: String = currentDateKey()): String {
    return today.substring(0, 7)
}

private fun currentDateKey(calendar: Calendar = Calendar.getInstance()): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = calendar.timeZone
    }.format(Date(calendar.timeInMillis))
}

private fun dayOfMonthFromDate(date: String): Int {
    return date.substring(8, 10).toInt()
}

private fun daysBetween(startDate: String, endDate: String): Int {
    val start = calendarFromDate(startDate)
    val end = calendarFromDate(endDate)
    val diff = end.timeInMillis - start.timeInMillis
    return (diff / MILLIS_PER_DAY).toInt()
}

private fun calendarFromDate(date: String): Calendar {
    return Calendar.getInstance().apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.YEAR, date.substring(0, 4).toInt())
        set(Calendar.MONTH, date.substring(5, 7).toInt() - 1)
        set(Calendar.DAY_OF_MONTH, date.substring(8, 10).toInt())
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

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
    return currentInventoryItems(state.inventory)
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
        InventoryCatalog.itemEnergyPotion to 3,
        InventoryCatalog.itemLuckyNote to 2,
        InventoryCatalog.itemLotteryTicket to demoAccountContext.lotteryTicketCount
    )
}

private fun syncInventoryState(state: PersistedAppState): PersistedAppState {
    val previousInventory = state.inventory
    val migrationApplied = previousInventory.metadata[InventoryCatalog.wardrobeMigrationVersion] == "true"

    val nextItems = previousInventory.items.toMutableMap()
    val defaultInventory = defaultInventoryState(state.lotteryTicketCount)
    val nextSkills = previousInventory.skillCards.toMutableMap()
    defaultInventory.skillCards.forEach { (skillId, skillState) ->
        nextSkills.putIfAbsent(skillId, skillState)
    }
    InventoryCatalog.itemDefinitions.forEach { (itemId, definition) ->
        val current = nextItems[itemId]
        val count = when (itemId) {
            InventoryCatalog.itemLotteryTicket -> state.lotteryTicketCount
            else -> state.itemCounts[itemId] ?: current?.count ?: 0
        }
        nextItems[itemId] = (current ?: ItemState(
            itemId = itemId,
            count = count,
            isConsumable = definition.isConsumable
        )).copy(
            count = count,
            isConsumable = definition.isConsumable
        )
    }

    val nextSkins = previousInventory.skins.toMutableMap()
    val nextShards = previousInventory.shards.toMutableMap()
    InventoryCatalog.skinDefinitions.forEach { (skinId, definition) ->
        val existingSkin = nextSkins[skinId]
        val existingShard = nextShards[skinId]
        val shouldResetShard = !migrationApplied && skinId in InventoryCatalog.migratedWardrobeSkinIds
        val shardCount = when {
            shouldResetShard -> 0
            existingShard != null -> existingShard.count
            existingSkin != null -> existingSkin.shardCount
            definition.defaultUnlocked -> definition.shardsRequired
            else -> 0
        }
        val fallbackUnlockSource = if (definition.defaultUnlocked) SkinUnlockSource.DEFAULT else null
        nextSkins[skinId] = (existingSkin ?: SkinState(
            skinId = skinId,
            isUnlocked = definition.defaultUnlocked,
            unlockSource = fallbackUnlockSource
        )).copy(
            isUnlocked = existingSkin?.isUnlocked ?: definition.defaultUnlocked,
            unlockSource = existingSkin?.unlockSource ?: fallbackUnlockSource,
            shardCount = shardCount,
            metadata = existingSkin?.metadata.orEmpty() + definition.metadata
        )
        nextShards[skinId] = (existingShard ?: ShardState(skinId = skinId, count = shardCount)).copy(
            count = shardCount
        )
    }

    val equippedSkinId = when {
        previousInventory.equippedSkinId != null &&
            nextSkins[previousInventory.equippedSkinId]?.isUnlocked == true -> previousInventory.equippedSkinId
        else -> nextSkins.entries.firstOrNull { it.value.isUnlocked }?.key ?: InventoryCatalog.skinCeremony
    }
    val now = System.currentTimeMillis()
    val normalizedSkins = nextSkins.mapValues { (skinId, skinState) ->
        val shouldEquip = equippedSkinId == skinId && skinState.isUnlocked
        if (skinState.isEquipped == shouldEquip) {
            skinState
        } else {
            skinState.copy(
                isEquipped = shouldEquip,
                equipTime = if (shouldEquip) (skinState.equipTime ?: now) else skinState.equipTime
            )
        }
    }

    val nextMetadata = previousInventory.metadata + mapOf(
        InventoryCatalog.wardrobeMigrationVersion to "true"
    )
    val nextInventory = previousInventory.copy(
        userId = demoAccountContext.characterId,
        skillCards = nextSkills,
        items = nextItems,
        skins = normalizedSkins,
        shards = nextShards,
        currencies = previousInventory.currencies +
            (CurrencyType.GACHA_TICKET to state.lotteryTicketCount) +
            (CurrencyType.STAR_DUST to state.stardustBalance),
        equippedSkinId = equippedSkinId,
        lastUpdateTime = now,
        metadata = nextMetadata
    )
    return if (
        nextInventory == previousInventory &&
        (state.itemCounts[InventoryCatalog.itemLotteryTicket] ?: 0) == state.lotteryTicketCount
    ) {
        state
    } else {
        state.copy(
            itemCounts = state.itemCounts + (InventoryCatalog.itemLotteryTicket to state.lotteryTicketCount),
            inventory = nextInventory
        )
    }
}

internal data class ShopPurchaseResult(
    val success: Boolean,
    val message: String
)

internal data class GachaActionResult(
    val success: Boolean,
    val message: String
)

internal data class GachaPullResult(
    val title: String,
    val detail: String,
    val rarity: String,
    val accentArgb: Int
)

private fun pickGachaReward(pool: GachaPoolPreview): GachaRewardPreview {
    val totalWeight = pool.rewards.sumOf { it.weight }
    val roll = Random.nextInt(totalWeight)
    var cursor = 0
    pool.rewards.forEach { reward ->
        cursor += reward.weight
        if (roll < cursor) return reward
    }
    return pool.rewards.last()
}

private fun applyGachaReward(
    state: PersistedAppState,
    reward: GachaRewardPreview
): Pair<PersistedAppState, GachaPullResult> {
    return when (reward.executionType) {
        GachaExecutionType.ITEM -> {
            val currentItem = state.inventory.items[reward.targetId]
            val nextCount = (currentItem?.count ?: 0) + 1
            val nextItems = state.inventory.items + (
                reward.targetId to (currentItem ?: ItemState(
                    itemId = reward.targetId,
                    count = nextCount,
                    isConsumable = InventoryCatalog.itemDefinitions[reward.targetId]?.isConsumable == true
                )).copy(count = nextCount)
            )
            state.copy(
                itemCounts = state.itemCounts + (reward.targetId to nextCount),
                inventory = state.inventory.copy(items = nextItems, lastUpdateTime = System.currentTimeMillis())
            ) to GachaPullResult(reward.targetName, "道具 +1", reward.rarity, gachaAccentForRarity(reward.rarity).value.toLong().toInt())
        }

        GachaExecutionType.TICKET -> {
            state.copy(lotteryTicketCount = state.lotteryTicketCount + 1) to
                GachaPullResult(reward.targetName, "抽卡券 +1", reward.rarity, gachaAccentForRarity(reward.rarity).value.toLong().toInt())
        }

        GachaExecutionType.SHARD -> {
            val currentShard = state.inventory.shards[reward.targetId]
            val nextCount = (currentShard?.count ?: 0) + 1
            val nextShards = state.inventory.shards + (
                reward.targetId to (currentShard ?: ShardState(skinId = reward.targetId, count = nextCount)).copy(
                    count = nextCount,
                    lastUpdateTime = System.currentTimeMillis()
                )
            )
            val currentSkin = state.inventory.skins[reward.targetId]
            val nextSkins = if (currentSkin != null) {
                state.inventory.skins + (reward.targetId to currentSkin.copy(shardCount = nextCount))
            } else {
                state.inventory.skins
            }
            state.copy(
                inventory = state.inventory.copy(shards = nextShards, skins = nextSkins, lastUpdateTime = System.currentTimeMillis())
            ) to GachaPullResult(reward.targetName, "碎片 +1", reward.rarity, gachaAccentForRarity(reward.rarity).value.toLong().toInt())
        }

        GachaExecutionType.STARDUST -> {
            val amount = Random.nextInt(reward.minAmount ?: 5, (reward.maxAmount ?: 100) + 1)
            state.copy(stardustBalance = state.stardustBalance + amount) to
                GachaPullResult(reward.targetName, "星尘 +$amount", reward.rarity, gachaAccentForRarity(reward.rarity).value.toLong().toInt())
        }

        GachaExecutionType.SKILL -> {
            val skillState = state.inventory.skillCards[reward.targetId]
            val isDuplicate = skillState != null
            if (isDuplicate) {
                val convertItemId = InventoryCatalog.skillDuplicateConversion[reward.rarity] ?: InventoryCatalog.itemEnergyPotion
                val currentItem = state.inventory.items[convertItemId]
                val nextCount = (currentItem?.count ?: 0) + 1
                val nextItems = state.inventory.items + (
                    convertItemId to (currentItem ?: ItemState(
                        itemId = convertItemId,
                        count = nextCount,
                        isConsumable = InventoryCatalog.itemDefinitions[convertItemId]?.isConsumable == true
                    )).copy(count = nextCount)
                )
                state.copy(
                    itemCounts = state.itemCounts + (convertItemId to nextCount),
                    inventory = state.inventory.copy(items = nextItems, lastUpdateTime = System.currentTimeMillis())
                ) to GachaPullResult(reward.targetName, "重复转 ${InventoryCatalog.itemDefinitions[convertItemId]?.name ?: convertItemId} +1", reward.rarity, gachaAccentForRarity(reward.rarity).value.toLong().toInt())
            } else {
                val nextSkills = state.inventory.skillCards + (
                    reward.targetId to com.maincharacter.shared.model.SkillCardState(
                        skillCardId = reward.targetId,
                        isUnlocked = true,
                        unlockTime = System.currentTimeMillis(),
                        level = 1,
                        experience = 0,
                        usageCount = 0
                    )
                )
                state.copy(
                    inventory = state.inventory.copy(skillCards = nextSkills, lastUpdateTime = System.currentTimeMillis())
                ) to GachaPullResult(reward.targetName, "新技能解锁", reward.rarity, gachaAccentForRarity(reward.rarity).value.toLong().toInt())
            }
        }
    }
}

private fun gachaAccentForRarity(rarity: String): Color {
    return when (rarity) {
        "SSR" -> Color(0xFFF7B6D1)
        "SR" -> Color(0xFFD6C7FF)
        "R" -> Color(0xFF9ED8FF)
        else -> Color(0xFFFFE37A)
    }
}

private fun parseRewardAmount(value: String): Int? {
    val normalized = value.trim().replace("＋", "+").replace("－", "-")
    return normalized.toIntOrNull()
}

private fun buildPurchaseMessage(product: ShopProductDefinition): String {
    val reward = when (product.rewardType) {
        ShopRewardType.ITEM -> "${product.name} +${product.rewardAmount}"
        ShopRewardType.SKIN_SHARD -> "${product.name} +${product.rewardAmount}"
        ShopRewardType.TICKET -> "抽卡券 +${product.rewardAmount}"
        ShopRewardType.STATUS -> "${product.rewardTargetId} +${product.rewardAmount}"
        ShopRewardType.STARDUST -> "星尘 +${product.rewardAmount}"
    }
    return "购买成功：$reward"
}

private const val TEST_STARDUST_BALANCE = 1_000_000
