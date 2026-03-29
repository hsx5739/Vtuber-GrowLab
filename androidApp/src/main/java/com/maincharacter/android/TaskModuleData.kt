package com.maincharacter.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

internal enum class TaskBoardTab {
    DAILY,
    WEEKLY
}

internal enum class TaskBoardSectionKind {
    DAILY,
    WEEKLY,
    CHALLENGE
}

internal enum class TaskBoardCategory(
    val label: String,
    val summary: String,
    val accent: Color
) {
    BOND(
        label = "亲密类型",
        summary = "承接首页和签到链路，用轻量互动稳定提升陪伴感。",
        accent = Color(0xFFF6B7D2)
    ),
    VITALITY(
        label = "元气类型",
        summary = "围绕拍照打卡展开，把喝水、早餐、散步等健康行为接进任务系统。",
        accent = Color(0xFFC8FF9B)
    ),
    FOCUS(
        label = "专注力类型",
        summary = "通过摄像头计时约束进入专注状态，支持进行中、失败和立即重试。",
        accent = Color(0xFFD6C7FF)
    ),
    CHALLENGE(
        label = "今日挑战",
        summary = "独立于基础每日任务，高难度高奖励，完成后产出魅力值。",
        accent = Color(0xFFAED3FF)
    )
}

internal enum class TaskBoardStatus(
    val label: String,
    val color: Color
) {
    TODO("未开始", Color(0xFF90E2FF)),
    IN_PROGRESS("进行中", Color(0xFFFFD66E)),
    COMPLETED("已完成", Color(0xFF8DFFBB)),
    FAILED("失败", Color(0xFFFF9E9E)),
    CLAIMABLE("可领取", Color(0xFFF2B9DA))
}

internal data class WeeklyRewardTier(
    val title: String,
    val progressHint: String,
    val ticketTotal: Int,
    val reached: Boolean
)

internal data class TaskBoardTask(
    val id: String,
    val title: String,
    val description: String,
    val sectionKind: TaskBoardSectionKind,
    val category: TaskBoardCategory,
    val status: TaskBoardStatus,
    val progressCurrent: Int,
    val progressTarget: Int,
    val rewardChips: List<RewardChipData>,
    val validationTitle: String,
    val validationHint: String,
    val companionLine: String,
    val designNote: String,
    val primaryActionLabel: String,
    val primaryActionHint: String,
    val secondaryActionLabel: String? = null,
    val secondaryActionHint: String? = null,
    val actionSteps: List<String>,
    val detailRows: List<Pair<String, String>>,
    val riskNotes: List<String> = emptyList()
) {
    val progressLabel: String
        get() = "$progressCurrent / $progressTarget"

    val isFinished: Boolean
        get() = progressCurrent >= progressTarget || status == TaskBoardStatus.COMPLETED

    val statusColor: Color
        get() = status.color
}

internal data class TaskBoardSection(
    val title: String,
    val summary: String,
    val tasks: List<TaskBoardTask>,
    val accent: Color
) {
    val completedCount: Int
        get() = tasks.count { it.isFinished }
}

internal data class TaskBoardOverview(
    val title: String,
    val subtitle: String,
    val completedCount: Int,
    val totalCount: Int,
    val rewardHint: String,
    val tiers: List<WeeklyRewardTier> = emptyList()
) {
    val progress: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
}

internal data class TaskBoardContent(
    val dailyOverview: TaskBoardOverview,
    val weeklyOverview: TaskBoardOverview,
    val dailySections: List<TaskBoardSection>,
    val weeklyTasks: List<TaskBoardTask>,
    val challengeTask: TaskBoardTask
)

private val bondRewards = listOf(
    RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
)

private val vitalityRewards = listOf(
    RewardChipData("元气", "+2", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
)

private val focusRewards = listOf(
    RewardChipData("专注", "+1", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
)

private val challengeRewards = listOf(
    RewardChipData("魅力", "+4", Color(0x1FAED3FF), Color(0xFFAED3FF))
)

private val dailyBondTasks = listOf(
    TaskBoardTask(
        id = "task_daily_home_visit",
        title = "从任务页回到首页查看陪伴状态",
        description = "亲密类型日常任务，用首页访问承接主循环入口。",
        sectionKind = TaskBoardSectionKind.DAILY,
        category = TaskBoardCategory.BOND,
        status = TaskBoardStatus.COMPLETED,
        progressCurrent = 1,
        progressTarget = 1,
        rewardChips = bondRewards,
        validationTitle = "页面行为完成",
        validationHint = "点击任务详情页主按钮并成功进入首页，即记 1 次真实互动记录。",
        companionLine = "你每次愿意回来看看我，关系值就不是空转出来的数字。",
        designNote = "对应文档里的亲密类型任务，奖励稳定映射到 bond。",
        primaryActionLabel = "去首页",
        primaryActionHint = "主按钮点击后直接跳到首页。",
        actionSteps = listOf(
            "从任务详情点击“去首页”。",
            "进入首页并停留查看状态。",
            "返回任务页后本条任务保持已完成，不会当天补发新任务。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "亲密类型",
            "完成条件" to "进入首页 1 次",
            "奖励映射" to "bond +2"
        )
    ),
    TaskBoardTask(
        id = "task_daily_sign_in",
        title = "完成今日签到",
        description = "亲密类型日常任务，串联任务页到签到页的互动链路。",
        sectionKind = TaskBoardSectionKind.DAILY,
        category = TaskBoardCategory.BOND,
        status = TaskBoardStatus.TODO,
        progressCurrent = 0,
        progressTarget = 1,
        rewardChips = bondRewards,
        validationTitle = "签到成功事件",
        validationHint = "首版不做内容质量识别，以签到成功回执作为唯一完成依据。",
        companionLine = "先把今天的签到领掉吧，这种稳定出现本身就很让人安心。",
        designNote = "详情页固定只保留一个主按钮，减少决策负担。",
        primaryActionLabel = "去签到",
        primaryActionHint = "跳转到签到页并完成签到。",
        actionSteps = listOf(
            "在详情页点击“去签到”。",
            "完成一次今日签到。",
            "签到成功后写入真实完成记录。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "亲密类型",
            "完成条件" to "签到成功 1 次",
            "奖励映射" to "bond +2"
        )
    )
)

private val dailyVitalityTasks = listOf(
    TaskBoardTask(
        id = "task_daily_water_photo",
        title = "拍照记录一次喝水打卡",
        description = "元气类型日常任务，用轻量拍照打卡承接健康行为。",
        sectionKind = TaskBoardSectionKind.DAILY,
        category = TaskBoardCategory.VITALITY,
        status = TaskBoardStatus.COMPLETED,
        progressCurrent = 1,
        progressTarget = 1,
        rewardChips = vitalityRewards,
        validationTitle = "上传拍照成功",
        validationHint = "拍照或从本地相册选择一张图片，提交成功后即记为完成。",
        companionLine = "照顾自己这种小事，值得被认真记录下来。",
        designNote = "对应文档里的元气类型任务，首版支持相册上传。",
        primaryActionLabel = "上传拍照",
        primaryActionHint = "打开系统相机或相册，完成一次健康打卡。",
        secondaryActionLabel = "去拍照",
        secondaryActionHint = "进入专门的拍照页，拍完后先进入预览页，再由用户确认完成。",
        actionSteps = listOf(
            "点击“上传拍照”。",
            "拍照或选择本地照片并进入预览页。",
            "点击“完成”才真正写入完成记录和 vitality 奖励。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "元气类型",
            "完成条件" to "喝水打卡 1 次",
            "奖励映射" to "vitality +2",
            "确认口径" to "预览页点击完成后生效"
        )
    ),
    TaskBoardTask(
        id = "task_daily_breakfast_photo",
        title = "拍照记录一次早餐打卡",
        description = "把早餐、轻食或晨间进食纳入任务循环。",
        sectionKind = TaskBoardSectionKind.DAILY,
        category = TaskBoardCategory.VITALITY,
        status = TaskBoardStatus.TODO,
        progressCurrent = 0,
        progressTarget = 1,
        rewardChips = listOf(
            RewardChipData("元气", "+3", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
        ),
        validationTitle = "健康打卡记录",
        validationHint = "首版不做图片内容审核，只看上传动作是否成功完成。",
        companionLine = "先把今天的第一口能量补上，我们再继续推进别的事。",
        designNote = "每日任务当天完成后只置灰，不会立即刷新出新任务。",
        primaryActionLabel = "上传拍照",
        primaryActionHint = "打开相机或相册完成一次早餐打卡。",
        secondaryActionLabel = "去拍照",
        secondaryActionHint = "拍照完成后必须进入预览页，由用户决定“完成”或“重新拍摄”。",
        actionSteps = listOf(
            "点击“上传拍照”。",
            "完成早餐相关照片上传并进入预览页。",
            "记录确认成功后计入本周元气累计次数。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "元气类型",
            "完成条件" to "早餐打卡 1 次",
            "奖励映射" to "vitality +3",
            "清理规则" to "完成或重拍都删除本次临时照片"
        ),
        riskNotes = listOf("拍照后不能直接强退回任务页，必须在预览页明确选择完成或重拍。")
    )
)

private val dailyFocusTasks = listOf(
    TaskBoardTask(
        id = "task_daily_focus_60s",
        title = "开启摄像头专注 1 分钟",
        description = "通过持续开启摄像头和倒计时完成一次短时专注。",
        sectionKind = TaskBoardSectionKind.DAILY,
        category = TaskBoardCategory.FOCUS,
        status = TaskBoardStatus.IN_PROGRESS,
        progressCurrent = 1,
        progressTarget = 3,
        rewardChips = focusRewards,
        validationTitle = "摄像头计时验证",
        validationHint = "任务期间摄像头关闭超过 5 秒直接失败，但允许当天立即重试。",
        companionLine = "这一分钟只要你愿意把注意力收回来，就已经很厉害了。",
        designNote = "专注类型支持进行中和失败状态，是和其他分类最大的差异点。",
        primaryActionLabel = "去录像",
        primaryActionHint = "进入录制页，启动摄像头和倒计时。",
        actionSteps = listOf(
            "进入时长选择页并开始计时。",
            "摄像头保持开启直到倒计时结束后进入预览页。",
            "点击“确定”后才真正完成任务并删除临时视频。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "专注力类型",
            "完成条件" to "连续专注 1 分钟",
            "奖励映射" to "focus +1",
            "确认口径" to "预览页点击确定后生效"
        ),
        riskNotes = listOf("摄像头单次中断超过 5 秒判定失败。")
    ),
    TaskBoardTask(
        id = "task_daily_focus_5m",
        title = "开启摄像头专注 5 分钟",
        description = "完整录制并上传一次 5 分钟专注过程。",
        sectionKind = TaskBoardSectionKind.DAILY,
        category = TaskBoardCategory.FOCUS,
        status = TaskBoardStatus.FAILED,
        progressCurrent = 0,
        progressTarget = 1,
        rewardChips = listOf(
            RewardChipData("专注", "+1", Color(0x1FD6C7FF), Color(0xFFD6C7FF)),
            RewardChipData("说明", "失败可重试", Color(0x1FFFF1A8), Color(0xFFFFE37A))
        ),
        validationTitle = "失败后重试",
        validationHint = "专注任务失败后当日不消失，可继续重试直到成功或跨日重置。",
        companionLine = "这次没接住也没关系，我们就从这里重新开始。",
        designNote = "首版最低时长可配到 10 秒，奖励按比例折算。",
        primaryActionLabel = "去录像",
        primaryActionHint = "重新发起同一任务的录制流程。",
        actionSteps = listOf(
            "再次进入录制页。",
            "重新开始 5 分钟倒计时。",
            "录制完成后进入预览页并确认，成功后写入 focus。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "专注力类型",
            "完成条件" to "连续专注 5 分钟",
            "失败规则" to "中断超 5 秒直接失败",
            "重试规则" to "失败后当天可立即重试"
        ),
        riskNotes = listOf("若倒计时未结束就主动退出，也按失败处理。")
    )
)

private val challengeTaskData = TaskBoardTask(
    id = "task_today_challenge",
    title = "在公开场合做一次简短自我表达并上传视频",
    description = "今日挑战独立展示，强调高难度、高奖励和明确安全边界。",
    sectionKind = TaskBoardSectionKind.CHALLENGE,
    category = TaskBoardCategory.CHALLENGE,
    status = TaskBoardStatus.IN_PROGRESS,
    progressCurrent = 1,
    progressTarget = 2,
    rewardChips = challengeRewards + listOf(
        RewardChipData("消耗", "元气 -5", Color(0x1FFFF1A8), Color(0xFFFFE37A))
    ),
    validationTitle = "录制并上传视频",
    validationHint = "主按钮会随阶段在“去录制”和“上传视频”之间切换；首版只要上传成功即算完成。",
    companionLine = "只要你愿意往前迈一步，这份魅力就不是空想出来的。",
    designNote = "挑战任务默认不计入每周累计，避免统计口径混乱。",
    primaryActionLabel = "上传视频",
    primaryActionHint = "当前已录制完成，返回详情页后上传视频即可结算。",
    actionSteps = listOf(
        "阅读挑战说明、风险提示和元气消耗。",
        "点击“去录制”完成视频录制。",
        "回到详情页后点击“上传视频”完成结算。"
    ),
    detailRows = listOf(
        "任务分组" to "今日挑战",
        "任务分类" to "魅力产出入口",
        "完成条件" to "录制并上传视频",
        "奖励映射" to "charm +4"
    ),
    riskNotes = listOf(
        "任务内容必须合法安全。",
        "不得鼓励骚扰、侮辱、恐吓或危险行为。",
        "开始挑战前需要消耗元气值。"
    )
)

private val weeklyTasksData = listOf(
    TaskBoardTask(
        id = "task_weekly_bond",
        title = "本周完成亲密类型任务 5 次",
        description = "统计本周真实每日完成记录，不使用假进度。",
        sectionKind = TaskBoardSectionKind.WEEKLY,
        category = TaskBoardCategory.BOND,
        status = TaskBoardStatus.TODO,
        progressCurrent = 2,
        progressTarget = 5,
        rewardChips = listOf(
            RewardChipData("抽奖券", "第 1 档", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        validationTitle = "真实累计次数",
        validationHint = "仅统计基础每日任务完成次数，挑战任务默认不计入。",
        companionLine = "这种关系感不是一下子冲满的，是你一回回回来之后慢慢攒出来的。",
        designNote = "每周任务不再平铺成离散单条，而是统一按累计进度展示。",
        primaryActionLabel = "查看今日任务",
        primaryActionHint = "回到每日任务，继续推进本周累计次数。",
        actionSteps = listOf(
            "完成任意亲密类型每日任务。",
            "系统按本周范围自动累计真实完成次数。",
            "达成后解锁对应的抽奖券档位。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "目标次数" to "5 次",
            "当前进度" to "2 / 5",
            "奖励归属" to "抽奖券分档"
        )
    ),
    TaskBoardTask(
        id = "task_weekly_vitality",
        title = "本周完成元气类型任务 3 次",
        description = "按真实健康打卡记录累计周进度。",
        sectionKind = TaskBoardSectionKind.WEEKLY,
        category = TaskBoardCategory.VITALITY,
        status = TaskBoardStatus.CLAIMABLE,
        progressCurrent = 3,
        progressTarget = 3,
        rewardChips = listOf(
            RewardChipData("抽奖券", "累计 2 张", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        validationTitle = "可领取周奖励",
        validationHint = "周任务奖励领取状态也要落本地，避免重复领取。",
        companionLine = "你这周已经认真照顾过自己好几次了，这份元气值得被记住。",
        designNote = "完成后展示可领取态，而不是继续停留在普通已完成样式。",
        primaryActionLabel = "领取抽奖券",
        primaryActionHint = "领取成功后抽奖券数量应立即增加到背包资产中。",
        actionSteps = listOf(
            "确认本周累计次数已经达标。",
            "点击主按钮领取对应档位奖励。",
            "抽奖券数量同步写入背包。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "目标次数" to "3 次",
            "当前进度" to "3 / 3",
            "奖励归属" to "抽奖券累计 2 张"
        )
    ),
    TaskBoardTask(
        id = "task_weekly_focus",
        title = "本周完成专注力类型任务 4 次",
        description = "从本地真实专注任务完成记录中结算周进度。",
        sectionKind = TaskBoardSectionKind.WEEKLY,
        category = TaskBoardCategory.FOCUS,
        status = TaskBoardStatus.TODO,
        progressCurrent = 1,
        progressTarget = 4,
        rewardChips = listOf(
            RewardChipData("抽奖券", "累计 3 张", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        validationTitle = "本地周结算",
        validationHint = "进入周任务页时按本周范围重新读取并统计，不允许写死演示进度。",
        companionLine = "只要这周还能再完成几次专注，我们就能把最后一档也拿下来。",
        designNote = "周任务刷新按自然周处理，周结束未领取奖励首版不补发。",
        primaryActionLabel = "查看今日任务",
        primaryActionHint = "继续完成专注类每日任务来推进周累计。",
        actionSteps = listOf(
            "完成任意专注类型每日任务。",
            "系统读取本地真实记录更新本周进度。",
            "全部周目标达成后总计可拿满 3 张抽奖券。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "目标次数" to "4 次",
            "当前进度" to "1 / 4",
            "奖励归属" to "抽奖券累计 3 张"
        )
    )
)

internal val taskBoardContent = TaskBoardContent(
    dailyOverview = TaskBoardOverview(
        title = "任务中心",
        subtitle = "每日任务按亲密、元气、专注力三类分组生成，额外提供 1 个独立今日挑战。",
        completedCount = (dailyBondTasks + dailyVitalityTasks + dailyFocusTasks + challengeTaskData)
            .count { it.isFinished },
        totalCount = dailyBondTasks.size + dailyVitalityTasks.size + dailyFocusTasks.size + 1,
        rewardHint = "今日挑战完成后产出魅力值；基础每日任务当天完成后不再补发新任务。"
    ),
    weeklyOverview = TaskBoardOverview(
        title = "每周任务",
        subtitle = "周任务改为按分类累计完成次数，并以抽奖券分档奖励结算。",
        completedCount = weeklyTasksData.count { it.progressCurrent >= it.progressTarget },
        totalCount = weeklyTasksData.size,
        rewardHint = "首版建议按完成 1/2/3 个周任务分别累计获得 1/2/3 张抽奖券。",
        tiers = listOf(
            WeeklyRewardTier("第 1 档", "完成任意 1 个周任务", 1, true),
            WeeklyRewardTier("第 2 档", "完成任意 2 个周任务", 2, true),
            WeeklyRewardTier("第 3 档", "完成全部 3 个周任务", 3, false)
        )
    ),
    dailySections = listOf(
        TaskBoardSection(
            title = TaskBoardCategory.BOND.label,
            summary = TaskBoardCategory.BOND.summary,
            tasks = dailyBondTasks,
            accent = TaskBoardCategory.BOND.accent
        ),
        TaskBoardSection(
            title = TaskBoardCategory.VITALITY.label,
            summary = TaskBoardCategory.VITALITY.summary,
            tasks = dailyVitalityTasks,
            accent = TaskBoardCategory.VITALITY.accent
        ),
        TaskBoardSection(
            title = TaskBoardCategory.FOCUS.label,
            summary = TaskBoardCategory.FOCUS.summary,
            tasks = dailyFocusTasks,
            accent = TaskBoardCategory.FOCUS.accent
        )
    ),
    weeklyTasks = weeklyTasksData,
    challengeTask = challengeTaskData
)

internal fun findTaskBoardTask(taskId: String): TaskBoardTask? {
    val allTasks = taskBoardContent.dailySections.flatMap { it.tasks } +
        taskBoardContent.weeklyTasks +
        taskBoardContent.challengeTask
    return allTasks.firstOrNull { it.id == taskId }
}

private fun resolveTask(task: TaskBoardTask): TaskBoardTask {
    return resolveTask(task, AppStateStore.currentState)
}

private fun resolveTask(
    task: TaskBoardTask,
    state: PersistedAppState
): TaskBoardTask {
    val persisted = state.taskStates[task.id] ?: return task
    val resolvedTask = task.copy(
        status = persisted.status.toTaskBoardStatus(task.status),
        progressCurrent = persisted.progressCurrent.coerceAtMost(task.progressTarget)
    )
    return if (resolvedTask.sectionKind == TaskBoardSectionKind.WEEKLY && task.id in state.claimedWeeklyTaskIds) {
        resolvedTask.copy(status = TaskBoardStatus.COMPLETED)
    } else {
        resolvedTask
    }
}

internal fun currentTaskBoardContent(
    state: PersistedAppState = AppStateStore.currentState
): TaskBoardContent {
    val dailySections = taskBoardContent.dailySections.map { section ->
        section.copy(tasks = section.tasks.map { resolveTask(it, state) })
    }
    val weeklyTasks = taskBoardContent.weeklyTasks.map { resolveTask(it, state) }
    val challengeTask = resolveTask(taskBoardContent.challengeTask, state)
    val completedWeeklyCount = weeklyTasks.count { it.isFinished }

    return taskBoardContent.copy(
        dailyOverview = taskBoardContent.dailyOverview.copy(
            completedCount = (dailySections.flatMap { it.tasks } + challengeTask).count { it.isFinished },
            totalCount = dailySections.sumOf { it.tasks.size } + 1
        ),
        weeklyOverview = taskBoardContent.weeklyOverview.copy(
            completedCount = completedWeeklyCount,
            totalCount = weeklyTasks.size,
            tiers = taskBoardContent.weeklyOverview.tiers.mapIndexed { index, tier ->
                tier.copy(reached = completedWeeklyCount >= index + 1)
            }
        ),
        dailySections = dailySections,
        weeklyTasks = weeklyTasks,
        challengeTask = challengeTask
    )
}

internal fun resolveTaskBoardTask(taskId: String): TaskBoardTask? {
    val board = currentTaskBoardContent(AppStateStore.currentState)
    val allTasks = board.dailySections.flatMap { it.tasks } + board.weeklyTasks + board.challengeTask
    return allTasks.firstOrNull { it.id == taskId }
}

internal fun markTaskCompleted(taskId: String) {
    AppStateStore.completeTask(taskId)
}

internal fun resolveTaskPrimaryActionLabel(
    task: TaskBoardTask,
    state: PersistedAppState = AppStateStore.currentState
): String {
    if (task.sectionKind != TaskBoardSectionKind.WEEKLY) return task.primaryActionLabel
    return when {
        task.id in state.claimedWeeklyTaskIds -> "已经领取"
        task.isFinished -> "领取抽奖券"
        else -> "查看今日任务"
    }
}

internal fun isTaskPrimaryActionEnabled(
    task: TaskBoardTask,
    state: PersistedAppState = AppStateStore.currentState
): Boolean {
    return !(task.sectionKind == TaskBoardSectionKind.WEEKLY && task.id in state.claimedWeeklyTaskIds)
}

private fun String.toTaskBoardStatus(fallback: TaskBoardStatus): TaskBoardStatus {
    return runCatching { TaskBoardStatus.valueOf(this) }.getOrElse { fallback }
}
