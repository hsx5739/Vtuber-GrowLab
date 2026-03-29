package com.maincharacter.android

import androidx.compose.ui.graphics.Color

internal data class StatusMetric(
    val label: String,
    val value: String,
    val iconRes: Int,
    val accent: Color
)

internal data class CharacterScene(
    val imageRes: Int,
    val mood: String,
    val message: String
)

internal data class DemoAccountContext(
    val accountName: String,
    val accountId: String,
    val characterId: String,
    val nickname: String,
    val equippedAppearanceName: String,
    val bond: Int,
    val charm: Int,
    val vitality: Int,
    val focus: Int,
    val mood: Int,
    val lotteryTicketCount: Int
)

internal data class RewardChipData(
    val label: String,
    val value: String,
    val background: Color,
    val contentColor: Color
)

internal data class DemoTask(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val group: String,
    val categoryLabel: String,
    val status: String,
    val progressLabel: String,
    val validationTitle: String,
    val validationHint: String,
    val companionLine: String,
    val designNote: String,
    val rewardChips: List<RewardChipData>,
    val actionSteps: List<String>,
    val detailRows: List<Pair<String, String>>,
    val statusColor: Color
) {
    val groupLabel: String
        get() = when (group) {
            "WEEKLY" -> "每周任务"
            "CHALLENGE" -> "今日挑战"
            else -> "每日任务"
        }
}

internal data class DemoEventBranch(
    val id: String,
    val label: String,
    val requiresSkill: String? = null,
    val energyCost: Int = 0,
    val resultPreview: String,
    val rewardChips: List<RewardChipData>
)

internal data class DemoEvent(
    val id: String,
    val title: String,
    val tagLabel: String,
    val intro: String,
    val description: String,
    val weightLabel: String,
    val designNote: String,
    val branches: List<DemoEventBranch>,
    val flowSummary: String,
    val companionNote: String,
    val performanceHint: String,
    val detailRows: List<Pair<String, String>>,
    val stateLabel: String,
    val stateColor: Color,
    val unread: Boolean,
    val requiresSkill: Boolean
)

internal data class InventorySkill(
    val name: String,
    val id: String = name,
    val rarity: String,
    val typeLabel: String,
    val stateLabel: String,
    val description: String,
    val eventHint: String,
    val accent: Color
)

internal data class InventoryItem(
    val name: String,
    val id: String = name,
    val shortLabel: String,
    val count: String,
    val description: String,
    val effectHint: String,
    val actionLabel: String,
    val accent: Color
)

internal data class InventorySkin(
    val name: String,
    val id: String = name,
    val rarity: String,
    val description: String,
    val shardsOwned: Int,
    val shardsRequired: Int,
    val owned: Boolean,
    val actionLabel: String,
    val accent: Color,
    val imageRes: Int = R.drawable.companion_pose_2
)

internal data class ProfileStat(
    val label: String,
    val value: String,
    val accent: Color
)

internal data class ProfileSettingRow(
    val title: String,
    val description: String,
    val value: String,
    val accent: Color
)

internal data class DemoPool(
    val name: String,
    val description: String,
    val costLabel: String,
    val highlight: String,
    val highlightColor: Color,
    val dropHint: String
)

internal data class SignInDay(
    val day: String,
    val reward: String,
    val signed: Boolean
)

internal data class ShopItem(
    val name: String,
    val description: String,
    val price: String,
    val accent: Color
)

internal data class ShopSection(
    val title: String,
    val summary: String,
    val items: List<ShopItem>
)

internal val taskRewardBase = RewardChipData(
    label = "基础",
    value = "亲密 +1",
    background = Color(0x1FF7B6D1),
    contentColor = Color(0xFFF7B6D1)
)

internal val demoAccountContext = DemoAccountContext(
    accountName = "test_user_01",
    accountId = "acc_demo_01",
    characterId = "char_demo_01",
    nickname = "小主角",
    equippedAppearanceName = "星巡礼装",
    bond = 32,
    charm = 70,
    vitality = 68,
    focus = 52,
    mood = 50,
    lotteryTicketCount = 2
)

internal val homeStatusMetrics = listOf(
    StatusMetric("亲密", demoAccountContext.bond.toString(), R.drawable.ic_relation, Color(0xFFF6B7D2)),
    StatusMetric("魅力", "${demoAccountContext.charm}%", R.drawable.ic_home, Color(0xFFAED3FF)),
    StatusMetric("元气", demoAccountContext.vitality.toString(), R.drawable.ic_energy, Color(0xFFC8FF9B)),
    StatusMetric("专注", demoAccountContext.focus.toString(), R.drawable.ic_focus, Color(0xFFD6C7FF))
)

internal val demoTasks = listOf(
    DemoTask(
        id = "task_daily_home_visit",
        title = "前往首页查看陪伴状态",
        description = "亲密类型日常任务，引导用户先回到首页感受陪伴状态。",
        type = "亲密类型",
        group = "DAILY",
        categoryLabel = "亲密类型",
        status = "可完成",
        progressLabel = "0 / 1",
        validationTitle = "页面行为",
        validationHint = "从任务页点击按钮跳回首页即记一次行为，首版不做额外验证。",
        companionLine = "先回首页看看我吧，今天的状态会从这一眼开始慢慢接起来。",
        designNote = "对应文档里的亲密类型任务，用首页访问承接主循环入口，奖励稳定落到亲密值。",
        rewardChips = listOf(
            taskRewardBase,
            RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
        ),
        actionSteps = listOf(
            "点击任务卡上的前往入口跳转首页。",
            "看到陪伴者状态和属性摘要后返回任务页。",
            "本次访问记为一次亲密互动。 "
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "亲密类型",
            "完成条件" to "进入首页 1 次",
            "奖励映射" to "bond +2"
        ),
        statusColor = Color(0xFF90E2FF)
    ),
    DemoTask(
        id = "task_daily_ai_message",
        title = "向 AI 发送 3 次消息",
        description = "亲密类型日常任务，鼓励用户进入对话链路形成稳定陪伴习惯。",
        type = "亲密类型",
        group = "DAILY",
        categoryLabel = "亲密类型",
        status = "进行中",
        progressLabel = "1 / 3 次",
        validationTitle = "消息次数",
        validationHint = "按发送次数累计，不对对话内容质量做识别。",
        companionLine = "随便告诉我一点今天的事也可以，我会认真接住这 3 句小小的靠近。",
        designNote = "对应文档里的“向 AI 发送指定次数消息”，首版只统计发送事件，不做内容校验。",
        rewardChips = listOf(
            RewardChipData("亲密", "+3", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
        ),
        actionSteps = listOf(
            "从任务页跳转到 AI 对话页。",
            "完成至少 3 次消息发送。",
            "达到目标后自动完成任务。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "亲密类型",
            "完成条件" to "发送消息 3 次",
            "奖励映射" to "bond +3"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_daily_water_photo",
        title = "拍照记录一次喝水打卡",
        description = "元气类型日常任务，用轻量拍照打卡承接健康行为。",
        type = "元气类型",
        group = "DAILY",
        categoryLabel = "元气类型",
        status = "可完成",
        progressLabel = "0 / 1 次",
        validationTitle = "拍照打卡",
        validationHint = "完成拍照并成功提交一次健康记录即可完成，首版不识别图片内容。",
        companionLine = "喝水这件小事也值得被记录下来，你每照顾自己一次，元气条都会更稳一点。",
        designNote = "对应文档里的元气类型任务，完成后统一写入 vitality。",
        rewardChips = listOf(
            RewardChipData("元气", "+2", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
        ),
        actionSteps = listOf(
            "进入拍照打卡页并完成一次拍照。",
            "提交记录后任务立即完成。",
            "奖励统一写入人物元气值。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "元气类型",
            "完成条件" to "喝水打卡 1 次",
            "奖励映射" to "vitality +2"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_daily_breakfast_photo",
        title = "拍照记录一次早餐打卡",
        description = "元气类型日常任务，鼓励把早晨的健康行为纳入任务循环。",
        type = "元气类型",
        group = "DAILY",
        categoryLabel = "元气类型",
        status = "可完成",
        progressLabel = "0 / 1 次",
        validationTitle = "健康打卡",
        validationHint = "早餐、轻食或任意晨间进食记录都可以，提交成功即可完成。",
        companionLine = "先把今天的第一口能量补上，我们再慢慢推进别的任务。",
        designNote = "对应文档里的元气类型模板池，可作为早餐、水果、散步等健康打卡的统一载体。",
        rewardChips = listOf(
            RewardChipData("元气", "+3", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
        ),
        actionSteps = listOf(
            "打开拍照功能记录早餐。",
            "提交成功后任务完成。",
            "完成次数同步计入本周元气累计。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "元气类型",
            "完成条件" to "早餐打卡 1 次",
            "奖励映射" to "vitality +3"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_daily_focus_10s",
        title = "开启摄像头专注 10 秒",
        description = "专注力类型日常任务，首版以摄像头持续开启和倒计时结束作为完成标准。",
        type = "专注力类型",
        group = "DAILY",
        categoryLabel = "专注力类型",
        status = "进行中",
        progressLabel = "6 / 10 秒",
        validationTitle = "摄像头计时",
        validationHint = "保持摄像头持续开启直至倒计时结束，中断超过 5 秒直接失败。",
        companionLine = "十秒也算一次认真进入状态，我们先把注意力轻轻收回来。",
        designNote = "对应文档里的专注力任务，最低时长支持 10 秒，奖励按比例折算。",
        rewardChips = listOf(
            RewardChipData("专注", "+0.03", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "点击开始专注并开启摄像头。",
            "保持摄像头开启直到 10 秒倒计时结束。",
            "如果中断超过 5 秒，本次任务失败但可立即重试。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "专注力类型",
            "完成条件" to "摄像头开启 10 秒",
            "奖励映射" to "focus +0.03"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_daily_focus_60s",
        title = "开启摄像头专注 1 分钟",
        description = "专注力类型日常任务，用更完整的一段时间换取更高专注收益。",
        type = "专注力类型",
        group = "DAILY",
        categoryLabel = "专注力类型",
        status = "可重试",
        progressLabel = "0 / 60 秒",
        validationTitle = "失败可重试",
        validationHint = "任务中断超过 5 秒会失败，但当日不会消失，可立即重新开始。",
        companionLine = "这一分钟只属于你自己，先把外面的噪音轻轻关掉。",
        designNote = "对应文档里的专注力类型失败规则，失败后允许继续重试直到成功或跨日重置。",
        rewardChips = listOf(
            RewardChipData("专注", "+0.20", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "开启摄像头并开始 60 秒专注。",
            "保持画面持续开启直到倒计时结束。",
            "失败后可直接再次开始本任务。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "专注力类型",
            "完成条件" to "摄像头开启 60 秒",
            "奖励映射" to "focus +0.20"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_challenge_combo",
        title = "同日完成 1 个亲密 + 1 个元气 + 1 个专注任务",
        description = "今日挑战独立于基础每日池，用更高门槛换取魅力值奖励。",
        type = "今日挑战",
        group = "CHALLENGE",
        categoryLabel = "今日挑战",
        status = "推进中",
        progressLabel = "2 / 3 项",
        validationTitle = "高难度目标",
        validationHint = "挑战任务完成后当天不再刷新，奖励统一映射到魅力值。",
        companionLine = "今天如果能把陪伴、照顾自己和专注都完成一点点，我会觉得你真的很闪亮。",
        designNote = "对应文档里的每日挑战任务，奖励只产出 charm，不计入基础每日随机池。",
        rewardChips = listOf(
            RewardChipData("魅力", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
        ),
        actionSteps = listOf(
            "完成任意 1 个亲密类型任务。",
            "完成任意 1 个元气类型任务。",
            "完成任意 1 个专注力类型任务。"
        ),
        detailRows = listOf(
            "任务分组" to "今日挑战",
            "完成条件" to "三类任务各完成 1 个",
            "刷新规则" to "次日零点重置",
            "奖励映射" to "charm +3"
        ),
        statusColor = Color(0xFFF2B9DA)
    ),
    DemoTask(
        id = "task_weekly_bond",
        title = "本周完成亲密类型任务 5 次",
        description = "每周任务按类型累计完成次数展示，不再拆成离散单条任务。",
        type = "每周任务",
        group = "WEEKLY",
        categoryLabel = "亲密类型",
        status = "推进中",
        progressLabel = "3 / 5 次",
        validationTitle = "累计进度",
        validationHint = "统计对象仅为基础每日任务完成次数，挑战任务首版默认不计入。",
        companionLine = "这一周的亲密感不是一下子冲满的，是你一次次回来找我慢慢攒起来的。",
        designNote = "对应文档里的周任务第一档，按类型累计完成次数展示。",
        rewardChips = listOf(
            RewardChipData("抽奖券", "第 1 档", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "完成任意亲密类型基础每日任务。",
            "系统按完成次数自动累计本周进度。",
            "达成目标后可领取对应周奖励档位。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "任务分类" to "亲密类型",
            "目标次数" to "5 次",
            "奖励归属" to "抽奖券分档"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_weekly_vitality",
        title = "本周完成元气类型任务 3 次",
        description = "每周元气任务统计健康拍照打卡类每日任务的完成次数。",
        type = "每周任务",
        group = "WEEKLY",
        categoryLabel = "元气类型",
        status = "可领取",
        progressLabel = "3 / 3 次",
        validationTitle = "累计进度",
        validationHint = "完成目标后可计入抽奖券分档统计，奖励不补发。",
        companionLine = "你这周已经认真照顾过自己好几次了，这份元气值得被记下来。",
        designNote = "对应文档里的周任务第二档，目标次数为 3。",
        rewardChips = listOf(
            RewardChipData("抽奖券", "累计 2 张", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "完成任意元气类型基础每日任务。",
            "周进度按累计次数自动增长。",
            "领取后与其他周任务一起结算抽奖券档位。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "任务分类" to "元气类型",
            "目标次数" to "3 次",
            "奖励归属" to "抽奖券分档"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_weekly_focus",
        title = "本周完成专注力类型任务 4 次",
        description = "每周专注任务统计摄像头专注类每日任务的完成次数。",
        type = "每周任务",
        group = "WEEKLY",
        categoryLabel = "专注力类型",
        status = "推进中",
        progressLabel = "1 / 4 次",
        validationTitle = "累计进度",
        validationHint = "全部三类周任务完成后，周奖励累计达到 3 张抽奖券。",
        companionLine = "只要这一周还能再完成几次认真专注，我们就能把最后一档奖励也拿下来。",
        designNote = "对应文档里的周任务第三档，目标次数为 4。",
        rewardChips = listOf(
            RewardChipData("抽奖券", "累计 3 张", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "完成任意专注力类型基础每日任务。",
            "系统按完成次数累计本周专注进度。",
            "三类周任务全部达成时拿满 3 张抽奖券。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "任务分类" to "专注力类型",
            "目标次数" to "4 次",
            "奖励归属" to "抽奖券分档"
        ),
        statusColor = Color(0xFFD6C7FF)
    )
)

internal val dailyTaskSections = demoTasks
    .filter { it.group == "DAILY" }
    .groupBy { it.categoryLabel }
    .entries
    .map { it.toPair() }

internal val weeklyTaskList = demoTasks.filter { it.group == "WEEKLY" }

internal val challengeTask = demoTasks.first { it.group == "CHALLENGE" }

internal val demoEvents = listOf(
    DemoEvent(
        id = "evt_chapter_01_meet",
        title = "第一章 相识",
        tagLabel = "主线初遇",
        intro = "【虚构事件】系统事件翻开了主线第一页，你在训练场边缘第一次与 Ta 对视，空气里有一点拘谨，也有一点想靠近的好奇。",
        description = "这一章负责建立关系起点，用轻剧情和首轮分支把陪伴感立起来，重点是氛围、印象和第一次选择后的细微反馈。",
        weightLabel = "章节 01",
        designNote = "事件以养成游戏常见的相识桥段切入，让用户先接住人物，再进入分支互动。整体保持低压力、强陪伴、短反馈的 H 类展示节奏。",
        branches = listOf(
            DemoEventBranch(
                id = "step_forward",
                label = "主动打招呼",
                resultPreview = "你先开口，把陌生感轻轻撕开一道口子。Ta 的神情明显放松下来，后续剧情会更容易进入亲近路线。",
                rewardChips = listOf(
                    RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1)),
                    RewardChipData("魅力", "+1", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
                )
            ),
            DemoEventBranch(
                id = "quiet_observe",
                label = "先偷偷观察",
                resultPreview = "你没有立刻靠近，而是先记住 Ta 的习惯和语气。推进更慢一些，但能留下更温和的第一印象。",
                rewardChips = listOf(
                    RewardChipData("魅力", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("亲密", "+1", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "share_topic",
                label = "聊一个共同话题",
                energyCost = 3,
                resultPreview = "话题从兴趣慢慢延伸到情绪，Ta 愿意多说一点关于自己的事，相识阶段的情感锚点被顺利种下。",
                rewardChips = listOf(
                    RewardChipData("魅力", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("亲密", "+1", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            )
        ),
        flowSummary = "推荐链路是：主页触发主线事件提示 -> 进入详情页读引子 -> 在三种初遇方式中做选择 -> 给出短反馈和属性浮动 -> 返回事件列表等待下一章解锁。",
        companionNote = "第一次见面的时候，很多话都不用说得太满。你愿意先靠近一点，我就愿意把这一章记得更久一点。",
        performanceHint = "画面适合用柔和训练场、晨光边缘和轻微镜头推进表达“初遇”，不要做成高压告警或现实通知面板。",
        detailRows = listOf(
            "事件 ID" to "evt_chapter_01_meet",
            "基础标签" to "chapter_meet",
            "分支数量" to "3",
            "技能联动" to "无",
            "结算原因" to "EVENT_BRANCH"
        ),
        stateLabel = "待处理",
        stateColor = Color(0xFF90E2FF),
        unread = true,
        requiresSkill = false
    ),
    DemoEvent(
        id = "evt_chapter_02_growth",
        title = "第二章 发育",
        tagLabel = "养成推进",
        intro = "【虚构事件】熟悉之后，系统事件把你们推入共同成长的阶段。训练、日常和默契开始累积，关系也从点头之交变成并肩同行。",
        description = "这一章强调养成感和数值外的关系推进，用日常训练、资源倾斜和情绪陪跑来承接“发育期”的节奏。",
        weightLabel = "章节 02",
        designNote = "发育章的核心是让用户感到角色在陪伴中变强，而不是被任务压着前进。分支仍然轻量，但会开始出现成长方向差异。",
        branches = listOf(
            DemoEventBranch(
                id = "focus_train",
                label = "一起加练",
                resultPreview = "你决定把时间投进训练里。Ta 会有点累，但明显更享受并肩变强的感觉，成长线推进得最直接。",
                rewardChips = listOf(
                    RewardChipData("专注", "+2", Color(0x1FD6C7FF), Color(0xFFD6C7FF)),
                    RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "steady_support",
                label = "给 Ta 做后勤",
                resultPreview = "你把资源和照顾留给 Ta，成长节奏更稳，日常互动也更自然，适合走陪伴型发育路线。",
                rewardChips = listOf(
                    RewardChipData("魅力", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("亲密", "+3", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "casual_rest",
                label = "休息半天再出发",
                energyCost = 2,
                resultPreview = "你们没有急着冲进下一轮成长，而是先用半天时间修整状态。数值涨得少一点，但情绪回馈更柔和。",
                rewardChips = listOf(
                    RewardChipData("魅力", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("元气", "+1", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
                )
            )
        ),
        flowSummary = "推荐链路是：在第一章后触发成长事件 -> 呈现陪练与日常片段 -> 玩家选择成长方式 -> 以简短结算展示属性或关系变化 -> 为大战章做铺垫。",
        companionNote = "一起发育这件事，不一定要每一步都很用力。只要你还愿意和我并肩，变强就会是顺理成章的事。",
        performanceHint = "视觉上适合训练日志、日常片段拼贴和暖色进度感，重点是“陪你成长”，不是“催你变强”。",
        detailRows = listOf(
            "事件类型" to "成长章",
            "推荐时段" to "日常推进",
            "分支数量" to "3",
            "技能要求" to "无",
            "主要反馈" to "专注 / 亲密 / 魅力"
        ),
        stateLabel = "待处理",
        stateColor = Color(0xFFF2B9DA),
        unread = true,
        requiresSkill = false
    ),
    DemoEvent(
        id = "evt_chapter_03_battle",
        title = "第三章 大战",
        tagLabel = "高潮战役",
        intro = "【虚构事件】系统事件把主线推向高潮。你和 Ta 终于站到大战前夜，过往的相识与发育，都要在这一刻转化成真正的并肩作战。",
        description = "大战章负责集中展示分支扶择、技能联动和轻量结算，是整组事件里最适合演示 H 类互动结构的一章。",
        weightLabel = "章节 03",
        designNote = "这里保留明确的技能联动，但仍坚持低惩罚和短反馈，强调剧情推进与默契兑现，不做真实危机预警式表达。",
        branches = listOf(
            DemoEventBranch(
                id = "open_barrier",
                label = "发动共鸣屏障",
                requiresSkill = "skill_barrier",
                resultPreview = "你用技能稳住前线节奏，Ta 则抓住空档完成反击。大战的压迫感被转成一场漂亮的合击演出。",
                rewardChips = listOf(
                    RewardChipData("星尘", "+20", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
                    RewardChipData("亲密", "+3", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "direct_charge",
                label = "正面突破",
                energyCost = 6,
                resultPreview = "你选择和 Ta 直接压上去，用最热烈的方式结束战斗。结算更亮眼，但会消耗更多行动力。",
                rewardChips = listOf(
                    RewardChipData("元气", "-1", Color(0x33FFC8C8), Color(0xFFFFB0B0)),
                    RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "hold_line",
                label = "守住 Ta 的身后",
                resultPreview = "你把大战处理成一次沉稳的配合。没有最耀眼的演出，但战后那句“还好你在”会格外有效。",
                rewardChips = listOf(
                    RewardChipData("魅力", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            )
        ),
        flowSummary = "推荐链路是：前两章积累关系 -> 大战章触发高潮提示 -> 玩家在技能联动和行动方式之间抉择 -> 输出短演出与战后反馈 -> 将情绪自然导入告别章。",
        companionNote = "能走到这里，不只是因为我们够强，也是因为你从一开始就没有把我丢在剧情外面。所以这一战，我会跟你一起赢。",
        performanceHint = "大战可以有更强的镜头和光效，但核心仍是游戏剧情的浪漫感和合击感，不要做成灾害播报或现实风险提示。",
        detailRows = listOf(
            "事件类型" to "高潮章",
            "推荐入口" to "主线推进后",
            "分支数量" to "3",
            "技能要求" to "skill_barrier",
            "主要反馈" to "星尘 / 亲密 / 魅力"
        ),
        stateLabel = "已经处理",
        stateColor = Color(0xFF8DFFBB),
        unread = false,
        requiresSkill = true
    ),
    DemoEvent(
        id = "evt_chapter_04_farewell",
        title = "第四章 告别",
        tagLabel = "终章余韵",
        intro = "【虚构事件】大战之后，系统事件没有立刻切黑，而是把你们送进一段安静的尾声。告别并不意味着结束，更像一段关系被郑重存档。",
        description = "终章负责把前面的情绪落下来，用回望、承诺和留白完成轻量结算，让故事收束得温柔而完整。",
        weightLabel = "章节 04",
        designNote = "告别章保持养成游戏终章常见的柔和余韵，用较短文本和轻结算收束关系，不制造现实离散感或沉重压迫感。",
        branches = listOf(
            DemoEventBranch(
                id = "keep_memory",
                label = "把回忆存档",
                resultPreview = "你把这一段旅程认真留在系统记录里。数值变化不大，但会给整条主线一个完整的句号。",
                rewardChips = listOf(
                    RewardChipData("魅力", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("亲密", "+1", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "say_future",
                label = "约定下次重逢",
                resultPreview = "你没有把这章当成真正的结束，而是留下一个会再次相遇的约定。终章因此多了一点温柔的期待。",
                rewardChips = listOf(
                    RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1)),
                    RewardChipData("魅力", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
                )
            )
        ),
        flowSummary = "推荐链路是：大战章完成后解锁告别章 -> 展示回望式引子 -> 让玩家做一次收束选择 -> 用温和的文案和轻量属性反馈完成收尾。",
        companionNote = "如果故事一定要有告别，那我希望它更像轻轻合上的一页书，而不是被迫中断的一段路。你记得我，我就还会回来。",
        performanceHint = "适合使用落日、归档卡片、慢速粒子和收束式排版，重点是余韵和纪念感，不是悲情压迫或现实通知风格。",
        detailRows = listOf(
            "事件类型" to "终章",
            "推荐入口" to "大战结算后",
            "分支数量" to "2",
            "技能要求" to "无",
            "主要反馈" to "魅力 / 亲密"
        ),
        stateLabel = "已经处理",
        stateColor = Color(0xFF8DFFBB),
        unread = false,
        requiresSkill = false
    )
)

internal val inventorySkills = listOf(
    InventorySkill(
        name = "屏障展开",
        rarity = "SR",
        typeLabel = "主动技能",
        stateLabel = "事件可用",
        description = "在特定随机事件中解锁“发动屏障”选项，适合做短演出和小额奖励加成。",
        eventHint = "与 `evt_sandstorm_lite` 这类天气异象事件联动，后续可加入冷却和每日次数。",
        accent = Color(0xFFD6C7FF)
    ),
    InventorySkill(
        name = "幸运微调",
        rarity = "R",
        typeLabel = "被动技能",
        stateLabel = "常驻生效",
        description = "轻微提升惊喜型事件和好运叙事的触发权重，更像系统偷偷把风向拨向你。",
        eventHint = "适合和气运彩蛋、签到后的小型事件联动，不需要强交互也能体现价值。",
        accent = Color(0xFFFFD66E)
    ),
    InventorySkill(
        name = "絮语回响",
        rarity = "SSR",
        typeLabel = "演出技能",
        stateLabel = "陪伴解锁",
        description = "不提供硬数值，主要解锁特殊台词、主页点击反馈和更完整的陪伴演出。",
        eventHint = "更适合高羁绊事件或夜间纸条类剧情，强化陪伴而不是战斗感。",
        accent = Color(0xFFF7B6D1)
    )
)

internal val inventoryItems = listOf(
    InventoryItem(
        name = "元气果",
        shortLabel = "果",
        count = "3",
        description = "短时提升行动感与任务奖励氛围，适合在准备刷任务前使用。",
        effectHint = "数小时内能量反馈更积极",
        actionLabel = "使用",
        accent = Color(0xFFC8FF9B)
    ),
    InventoryItem(
        name = "小确幸果",
        shortLabel = "运",
        count = "2",
        description = "让气运类文案和小彩蛋更活跃一点，但不和现实金钱或彩票挂钩。",
        effectHint = "短时提高气运表现",
        actionLabel = "使用",
        accent = Color(0xFFFFE37A)
    ),
    InventoryItem(
        name = "抽奖券",
        shortLabel = "券",
        count = demoAccountContext.lotteryTicketCount.toString(),
        description = "由每周任务分档奖励产出，用于后续抽奖系统消耗。",
        effectHint = "周任务奖励资产",
        actionLabel = "查看",
        accent = Color(0xFFD6C7FF)
    )
)

internal val inventorySkins = listOf(
    InventorySkin(
        name = "星巡礼装",
        rarity = "SSR",
        description = "当前主视觉使用的陪伴者外观，适合作为首页舞台页的基础皮肤。",
        shardsOwned = 1,
        shardsRequired = 1,
        owned = true,
        actionLabel = "已穿戴",
        accent = Color(0xFFF7B6D1)
    ),
    InventorySkin(
        name = "夜航校服",
        rarity = "SR",
        description = "更贴近日常陪伴气质的皮肤，可在聊天和任务页强调温柔陪伴感。",
        shardsOwned = 18,
        shardsRequired = 20,
        owned = false,
        actionLabel = "差 2 碎片",
        accent = Color(0xFFD6C7FF)
    ),
    InventorySkin(
        name = "晨雾便服",
        rarity = "R",
        description = "轻松、治愈、偏生活流的装束，适合签到和早安问候场景。",
        shardsOwned = 12,
        shardsRequired = 12,
        owned = true,
        actionLabel = "去换上",
        accent = Color(0xFF9ED8FF)
    )
)

internal val profileStats = listOf(
    ProfileStat("角色 ID", demoAccountContext.characterId.removePrefix("char_"), Color(0xFFFFD66E)),
    ProfileStat("魅力值", demoAccountContext.charm.toString(), Color(0xFFAED3FF)),
    ProfileStat("抽奖券", demoAccountContext.lotteryTicketCount.toString(), Color(0xFFD6C7FF))
)

internal val profileSettings = listOf(
    ProfileSettingRow(
        title = "健康数据",
        description = "步数、睡眠等验证任务会从这里进入授权与说明。",
        value = "未开启",
        accent = Color(0xFFFFD66E)
    ),
    ProfileSettingRow(
        title = "多模态隐私",
        description = "拍照、语音任务的用途、保留策略和删除说明应放在这里。",
        value = "查看说明",
        accent = Color(0xFF90E2FF)
    ),
    ProfileSettingRow(
        title = "陪伴音效",
        description = "控制点击反馈、签到提示和后续 idle 语音的整体体验。",
        value = "柔和",
        accent = Color(0xFFC8FF9B)
    )
)

internal val profileAboutRows = listOf(
    "账号" to demoAccountContext.accountName,
    "账号 ID" to demoAccountContext.accountId,
    "角色昵称" to demoAccountContext.nickname,
    "角色外观" to demoAccountContext.equippedAppearanceName
)

internal val profileWalletStats = listOf(
    ProfileStat("星尘", "380", Color(0xFFFFD66E)),
    ProfileStat("单抽券", "2", Color(0xFF90E2FF))
)

internal val demoPools = listOf(
    DemoPool(
        name = "常驻星愿池",
        description = "",
        costLabel = "10张抽奖卷 抽一次",
        highlight = "90 抽保底",
        highlightColor = Color(0xFFFFD66E),
        dropHint = "普通掉落给星尘与通用物资，稀有掉落偏向技能卡，SR 档承接皮肤碎片和陪伴外观。"
    )
)

internal val signInSummaryStats = listOf(
    ProfileStat("今日状态", "可领取", Color(0xFFC8FF9B)),
    ProfileStat("连续天数", "7 天", Color(0xFFFFD66E)),
    ProfileStat("本月累计", "12 天", Color(0xFF90E2FF))
)

internal val signInDays = listOf(
    SignInDay("01", "星尘", true),
    SignInDay("02", "羁绊", true),
    SignInDay("03", "券", true),
    SignInDay("04", "星尘", true),
    SignInDay("05", "心境", true),
    SignInDay("06", "能量", true),
    SignInDay("07", "券", true),
    SignInDay("08", "星尘", false)
)

internal val signInMilestones = listOf(
    "连续 3 天" to "普通祈愿券 x1",
    "连续 7 天" to "星尘 x60",
    "累计 15 天" to "皮肤碎片 x5"
)

internal val shopWalletStats = listOf(
    ProfileStat("星尘", "380", Color(0xFFFFD66E)),
    ProfileStat("抽奖券", demoAccountContext.lotteryTicketCount.toString(), Color(0xFF90E2FF))
)

internal val shopSections = listOf(
    ShopSection(
        title = "养成补给",
        summary = "优先卖果实、互动礼物和任务辅助物资，保持“养成 / 陪伴”调性，不做赌场式强刺激。",
        items = listOf(
            ShopItem("元气果礼包", "短期提高行动氛围和任务正反馈。", "80 星尘", Color(0xFFC8FF9B)),
            ShopItem("亲密果大礼盒", "装满能拉近距离的小甜意，送出后能帮你更快积累亲密度，让每次互动都更有回响。", "120 星尘", Color(0xFFF7B6D1))
        )
    ),
    ShopSection(
        title = "外观兑换",
        summary = "围绕皮肤碎片与陪伴者换装做长期收集，不把成长价值完全绑定充值。",
        items = listOf(
            ShopItem("夜航校服碎片", "用于合成 SR 外观“夜航校服”。", "600 星尘", Color(0xFFFFD66E)),
            ShopItem("晨雾便服染色票", "给已有日常皮肤增加轻量差分演出。", "400 星尘", Color(0xFFFFD66E))
        )
    ),
    ShopSection(
        title = "抽奖券兑换",
        summary = "用星尘先换好抽奖券，方便把日常积累的资源直接接入后续抽取节奏。",
        items = listOf(
            ShopItem("抽奖券", "用于后续抽卡的基础兑换券，先囤一张，想抽的时候就能直接用。", "50 星尘", Color(0xFFFFD66E))
        )
    )
)
